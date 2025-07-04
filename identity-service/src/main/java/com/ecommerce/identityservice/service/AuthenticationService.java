package com.ecommerce.identityservice.service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

import com.ecommerce.identityservice.dto.request.AuthenticationRequest;
import com.ecommerce.identityservice.exception.AppException;
import com.ecommerce.identityservice.exception.ErrorCode;
import com.ecommerce.identityservice.repository.InvalidatedTokenRepository;
import com.ecommerce.identityservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.ecommerce.identityservice.dto.request.IntrospectRequest;
import com.ecommerce.identityservice.dto.request.LogoutRequest;
import com.ecommerce.identityservice.dto.request.RefreshRequest;
import com.ecommerce.identityservice.dto.response.AuthenticationResponse;
import com.ecommerce.identityservice.dto.response.IntrospectResponse;
import com.ecommerce.identityservice.entity.InvalidatedToken;
import com.ecommerce.identityservice.entity.User;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    UserRepository userRepository;
    InvalidatedTokenRepository invalidatedTokenRepository;

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected long REFRESHABLE_DURATION;

    @Transactional(readOnly = true) // Keep if using Option 1 from previous fix
    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        var token = request.getToken();
        boolean isValid = true;

        try {
            // verifyToken might throw AppException, ParseException, or JOSEException
            verifyToken(token, false);
        } catch (AppException e) {
            // Catches our custom authentication/validation errors (like invalidated token)
            log.warn("Introspection failed: {}", e.getMessage());
            isValid = false;
        } catch (ParseException e) {
            // Catches errors during SignedJWT.parse() if the token format is invalid
            log.warn("Introspection failed due to invalid token format: {}", e.getMessage());
            isValid = false;
        } catch (JOSEException e) {
            // Catches errors during signature verification (e.g., MACVerifier failure)
            log.warn("Introspection failed due to JOSE verification error: {}", e.getMessage());
            isValid = false;
        }


        return IntrospectResponse.builder().valid(isValid).build();
    }
    @Transactional(readOnly = true)
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        var user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (!authenticated) throw new AppException(ErrorCode.UNAUTHENTICATED);

        var token = generateToken(user);

        return AuthenticationResponse.builder().token(token).authenticated(true).build();
    }

    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        try {
            var signToken = verifyToken(request.getToken(), true);

            String jit = signToken.getJWTClaimsSet().getJWTID();
            Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();

            InvalidatedToken invalidatedToken =
                    InvalidatedToken.builder().id(jit).expiryTime(expiryTime).build();

            invalidatedTokenRepository.save(invalidatedToken);
        } catch (AppException e) {

            log.info("Logout attempt for already invalid/expired token: {}", e.getMessage());
        } catch (ParseException | JOSEException e) {

            log.warn("Logout attempt with malformed token or verification failure: {}", e.getMessage());
        }

    }

    @Transactional(readOnly = true)
    public AuthenticationResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException {
        SignedJWT signedJWT;
        try {
            // Attempt to verify the token, catching potential parsing/verification errors
            signedJWT = verifyToken(request.getToken(), true);

        } catch (AppException e) {
            // If verifyToken itself throws AppException (e.g., expired, invalidated), rethrow it.
            throw e;
        } catch (ParseException | JOSEException e) {
            // If parsing or signature verification fails, wrap in standard AppException
            log.warn("Refresh token verification failed (parse/jose): {}", e.getMessage());
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        // --- Token is verified and parsed successfully at this point ---

        var jit = signedJWT.getJWTClaimsSet().getJWTID();
        var expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        InvalidatedToken invalidatedToken =
                InvalidatedToken.builder().id(jit).expiryTime(expiryTime).build();

        invalidatedTokenRepository.save(invalidatedToken);

        var userId = signedJWT.getJWTClaimsSet().getSubject();

        var user =
                userRepository.findByUserId(userId)
                        .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED)); // Keep this check

        var token = generateToken(user);

        return AuthenticationResponse.builder().token(token).authenticated(true).build();
    }


    public String generateToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUserId())
                .issuer("ecommerce.com")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(user))
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create token", e);
            throw new RuntimeException(e);
        }
    }

    public SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = (isRefresh)
                ? new Date(signedJWT
                .getJWTClaimsSet()
                .getIssueTime()
                .toInstant()
                .plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS)
                .toEpochMilli())
                : signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified = signedJWT.verify(verifier);

        if (!(verified && expiryTime.after(new Date()))) throw new AppException(ErrorCode.UNAUTHENTICATED);

        if (invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID()))
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        return signedJWT;
    }

    private String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");

        if (!CollectionUtils.isEmpty(user.getRoles()))
            user.getRoles().forEach(role -> {
                stringJoiner.add("ROLE_" + role.getName());
                if (!CollectionUtils.isEmpty(role.getPermissions()))
                    role.getPermissions().forEach(permission -> stringJoiner.add(permission.getName()));
            });

        return stringJoiner.toString();
    }
}