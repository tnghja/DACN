package com.ecommerce.identityservice.service;

import com.ecommerce.identityservice.dto.request.AuthenticationRequest;
import com.ecommerce.identityservice.dto.request.IntrospectRequest;
import com.ecommerce.identityservice.dto.request.LogoutRequest;
import com.ecommerce.identityservice.dto.request.RefreshRequest;
import com.ecommerce.identityservice.dto.request.UserCreationRequest;
import com.ecommerce.identityservice.dto.response.AuthenticationResponse;
import com.ecommerce.identityservice.dto.response.IntrospectResponse;
import com.ecommerce.identityservice.dto.response.UserResponse;
import com.ecommerce.identityservice.service.UserService;
import com.ecommerce.identityservice.service.PasswordResetService;
import com.ecommerce.identityservice.exception.AppException;
import com.ecommerce.identityservice.exception.ErrorCode;
import com.ecommerce.identityservice.repository.InvalidatedTokenRepository;
import com.ecommerce.identityservice.repository.UserRepository;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class AuthenticationServiceIntegrationTest {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InvalidatedTokenRepository invalidatedTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // Inject PasswordEncoder for setup

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordResetService passwordResetService;

    private final String TEST_EMAIL = "testauth@example.com";
    private final String TEST_PASSWORD = "password123";
    private final String TEST_USERNAME = "testauthuser";
    private String testUserId;

    @BeforeEach
    void setUp() {
        // Clean up before each test
        userRepository.findByEmail(TEST_EMAIL).ifPresent(user -> {
            invalidatedTokenRepository.deleteAll(); // Clear related tokens if necessary
            userRepository.delete(user);
        });
        userRepository.flush(); // Ensure deletion is committed

        // Arrange: Create a user for testing using the repository directly
        // or via UserService if complex setup/validation is needed
        com.ecommerce.identityservice.entity.User testUser = com.ecommerce.identityservice.entity.User.builder()
                .email(TEST_EMAIL)
                .userName(TEST_USERNAME)
                .password(passwordEncoder.encode(TEST_PASSWORD)) // Encode password
                .build();
        testUser = userRepository.saveAndFlush(testUser);
        testUserId = testUser.getUserId(); // Store the generated ID
    }

    @AfterEach
    void tearDown() {
        // Clean up after each test
        invalidatedTokenRepository.deleteAll();
        userRepository.findByEmail(TEST_EMAIL).ifPresent(userRepository::delete);
        userRepository.flush();
    }

    // --- TC-01: Đăng ký thành công với đầy đủ thông tin hợp lệ ---
    @Test
    @DisplayName("TC-01: Đăng ký thành công với đầy đủ thông tin hợp lệ")
    void testRegister_Success_TC01() {
        UserCreationRequest request = UserCreationRequest.builder()
                .email("newuser@example.com")
                .userName("newuser")
                .password("validPassword1")
                .build();
        UserResponse response = userService.createUser(request);
        assertNotNull(response.getUserId());
        assertEquals("newuser@example.com", response.getEmail());
        assertEquals("newuser", response.getUserName());// Password should not be exposed
    }

    // --- TC-02: Đăng ký với email đã tồn tại ---
    @Test
    @DisplayName("TC-02: Đăng ký với email đã tồn tại trong hệ thống")
    void testRegister_EmailExists_TC02() {
        userRepository.findByEmail(TEST_EMAIL).ifPresent(userRepository::delete);
        UserCreationRequest request = UserCreationRequest.builder()
                .email(TEST_EMAIL)
                .userName(TEST_USERNAME)
                .password(TEST_PASSWORD)
                .build();
        userService.createUser(request); // First registration
        AppException ex = assertThrows(AppException.class, () -> userService.createUser(request));
        assertEquals(ErrorCode.USER_EXISTED, ex.getErrorCode());
    }

    // --- TC-03: Đăng ký thiếu trường bắt buộc ---
    @Test
    @DisplayName("TC-03: Đăng ký thiếu trường bắt buộc (password)")
    void testRegister_MissingPassword_TC03() {
        UserCreationRequest request = UserCreationRequest.builder()
                .email("missingpass@example.com")
                .userName("missingpass")
                .password(null)
                .build();
        assertThrows(Exception.class, () -> userService.createUser(request));
    }

    // --- TC-04: Đăng ký với email không hợp lệ ---
    @Test
    @DisplayName("TC-04: Đăng ký với email không hợp lệ")
    void testRegister_InvalidEmail_TC04() {
        userRepository.findByEmail("invalid-email").ifPresent(userRepository::delete);
        UserCreationRequest request = UserCreationRequest.builder()
                .email("invalid-email")
                .userName("invalidemail")
                .password("password123")
                .build();
        assertThrows(AppException.class, () -> userService.createUser(request));
    }

    // --- TC-05: Đăng ký với password quá ngắn ---
    @Test
    @DisplayName("TC-05: Đăng ký với password quá ngắn")
    void testRegister_ShortPassword_TC05() {
        userRepository.findByEmail("shortpass@example.com").ifPresent(userRepository::delete);
        UserCreationRequest request = UserCreationRequest.builder()
                .email("shortpass@example.com")
                .userName("shortpass")
                .password("123")
                .build();
        assertThrows(AppException.class, () -> userService.createUser(request));
    }

    // --- TC-06: Đăng nhập thành công ---
    @Test
    @DisplayName("TC-06: Đăng nhập thành công với email và mật khẩu hợp lệ")
    void testLogin_Success_TC06() {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email(TEST_EMAIL)
                .password(TEST_PASSWORD)
                .build();
        AuthenticationResponse response = authenticationService.authenticate(request);
        assertNotNull(response.getToken());
        assertTrue(response.isAuthenticated());
    }

    // --- TC-07: Đăng nhập với mật khẩu sai ---
    @Test
    @DisplayName("TC-07: Đăng nhập với mật khẩu sai")
    void testLogin_WrongPassword_TC07() {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email(TEST_EMAIL)
                .password("wrongpassword")
                .build();
        AppException ex = assertThrows(AppException.class, () -> authenticationService.authenticate(request));
        assertEquals(ErrorCode.UNAUTHENTICATED, ex.getErrorCode());
    }

    // --- TC-08: Đăng nhập với email không tồn tại ---
    @Test
    @DisplayName("TC-08: Đăng nhập với email không tồn tại")
    void testLogin_EmailNotExist_TC08() {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("notfound@example.com")
                .password("anyPassword")
                .build();
        AppException ex = assertThrows(AppException.class, () -> authenticationService.authenticate(request));
        assertEquals(ErrorCode.USER_NOT_EXISTED, ex.getErrorCode());
    }

    // --- TC-09: Đăng nhập thiếu email/mật khẩu ---
    @Test
    @DisplayName("TC-09: Đăng nhập thiếu email hoặc mật khẩu")
    void testLogin_MissingFields_TC09() {
        AuthenticationRequest req1 = AuthenticationRequest.builder().email(null).password(TEST_PASSWORD).build();
        AuthenticationRequest req2 = AuthenticationRequest.builder().email(TEST_EMAIL).password(null).build();
        assertThrows(Exception.class, () -> authenticationService.authenticate(req1));
        assertThrows(Exception.class, () -> authenticationService.authenticate(req2));
    }

    // --- TC-10: Đăng nhập ngay sau khi đăng ký thành công ---
    @Test
    @DisplayName("TC-10: Đăng nhập ngay sau khi đăng ký thành công")
    void testLogin_AfterRegister_TC10() {
        UserCreationRequest registerRequest = UserCreationRequest.builder()
                .email("loginafterreg@example.com")
                .userName("loginafterreg")
                .password("passwordAfterReg")
                .build();
        userService.createUser(registerRequest);
        AuthenticationRequest loginRequest = AuthenticationRequest.builder()
                .email("loginafterreg@example.com")
                .password("passwordAfterReg")
                .build();
        AuthenticationResponse response = authenticationService.authenticate(loginRequest);
        assertNotNull(response.getToken());
        assertTrue(response.isAuthenticated());
    }

    // --- TC-11: Kiểm tra claims của JWT ---
    @Test
    @DisplayName("TC-11: Kiểm tra claims của JWT trả về")
    void testJWTClaims_TC11() throws Exception {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email(TEST_EMAIL)
                .password(TEST_PASSWORD)
                .build();
        AuthenticationResponse response = authenticationService.authenticate(request);
        String token = response.getToken();
        SignedJWT jwt = SignedJWT.parse(token);
        assertNotNull(jwt.getJWTClaimsSet().getSubject());
        assertNotNull(jwt.getJWTClaimsSet().getIssuer());
        assertNotNull(jwt.getJWTClaimsSet().getExpirationTime());
        assertNotNull(jwt.getJWTClaimsSet().getJWTID());
        assertNotNull(jwt.getJWTClaimsSet().getClaim("scope"));
    }

    // --- TC-12: Yêu cầu đặt lại mật khẩu với email hợp lệ ---
    @Test
    @DisplayName("TC-12: Yêu cầu đặt lại mật khẩu với email hợp lệ")
    void testRequestPasswordReset_ValidEmail_TC12() {
        boolean sent = passwordResetService.requestPasswordReset(TEST_EMAIL);
        assertTrue(sent);
    }

    // --- TC-13: Yêu cầu đặt lại mật khẩu với email không tồn tại ---
    @Test
    @DisplayName("TC-13: Yêu cầu đặt lại mật khẩu với email không tồn tại")
    void testRequestPasswordReset_EmailNotExist_TC13() {
        boolean sent = passwordResetService.requestPasswordReset("nouser@example.com");
        assertFalse(sent);
    }

    // --- TC-14: Yêu cầu đặt lại mật khẩu với email không đúng định dạng ---
    @Test
    @DisplayName("TC-14: Yêu cầu đặt lại mật khẩu với email không đúng định dạng")
    void testRequestPasswordReset_InvalidEmail_TC14() {
        boolean sent = passwordResetService.requestPasswordReset("bademail");
        assertFalse(sent);
    }

    // --- TC-15: Xác thực token reset hợp lệ và còn hạn ---
    @Test
    @DisplayName("TC-15: Xác thực token reset hợp lệ và còn hạn")
    void testValidateResetToken_Valid_TC15() throws Exception {
        passwordResetService.requestPasswordReset(TEST_EMAIL);
        String token = null;
        for (java.lang.reflect.Field field : passwordResetService.getClass().getDeclaredFields()) {
            if (field.getType().getName().contains("ConcurrentHashMap")) {
                field.setAccessible(true);
                java.util.concurrent.ConcurrentHashMap<?, ?> map = (java.util.concurrent.ConcurrentHashMap<?, ?>) field.get(passwordResetService);
                for (Object key : map.keySet()) {
                    if (passwordResetService.validateResetToken((String) key)) {
                        token = (String) key;
                        break;
                    }
                }
            }
        }
        assertNotNull(token);
        assertTrue(passwordResetService.validateResetToken(token));
    }

    // --- TC-16: Gửi mật khẩu mới với token hợp lệ ---
    @Test
    @DisplayName("TC-16: Gửi mật khẩu mới với token hợp lệ và mật khẩu mới")
    void testResetPassword_ValidToken_TC16() throws Exception {
        // Request reset to generate token
        passwordResetService.requestPasswordReset(TEST_EMAIL);
        String token = null;
        for (java.lang.reflect.Field field : passwordResetService.getClass().getDeclaredFields()) {
            if (field.getType().getName().contains("ConcurrentHashMap")) {
                field.setAccessible(true);
                java.util.concurrent.ConcurrentHashMap<?, ?> map = (java.util.concurrent.ConcurrentHashMap<?, ?>) field.get(passwordResetService);
                for (Object key : map.keySet()) {
                    if (passwordResetService.validateResetToken((String) key)) {
                        token = (String) key;
                        break;
                    }
                }
            }
        }
        assertNotNull(token);
        boolean changed = passwordResetService.resetPassword(token, "newPassword123");
        assertTrue(changed);
        // Now login with new password
        AuthenticationRequest loginRequest = AuthenticationRequest.builder()
                .email(TEST_EMAIL)
                .password("newPassword123")
                .build();
        AuthenticationResponse response = authenticationService.authenticate(loginRequest);
        assertNotNull(response.getToken());
        assertTrue(response.isAuthenticated());
        // Old password should now fail
        AuthenticationRequest oldLogin = AuthenticationRequest.builder()
                .email(TEST_EMAIL)
                .password(TEST_PASSWORD)
                .build();
        assertThrows(AppException.class, () -> authenticationService.authenticate(oldLogin));
    }

    // --- TC-17: Gửi mật khẩu mới với token không hợp lệ/hết hạn ---
    @Test
    @DisplayName("TC-17: Gửi mật khẩu mới với token không hợp lệ hoặc đã hết hạn")
    void testResetPassword_InvalidToken_TC17() {
        boolean changed = passwordResetService.resetPassword("invalid-token", "irrelevant");
        assertFalse(changed);
    }
    

    @Test
    @DisplayName("Authenticate: Success with Valid Credentials")
    void authenticate_validCredentials_returnsAuthenticatedResponse() {
        // Arrange
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email(TEST_EMAIL)
                .password(TEST_PASSWORD) // Use raw password for request
                .build();

        // Act
        AuthenticationResponse response = authenticationService.authenticate(request);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertNotNull(response.getToken(), "Token should not be null");
        assertTrue(response.isAuthenticated(), "Authentication status should be true");
        assertThat(response.getToken()).isNotBlank();
    }

    @Test
    @DisplayName("Authenticate: Fail on Non-Existent User")
    void authenticate_nonExistentUser_throwsAppException() {
        // Arrange
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("nonexistent@example.com")
                .password("password")
                .build();

        // Act & Assert
        AppException exception = assertThrows(AppException.class,
                () -> authenticationService.authenticate(request),
                "Should throw AppException for non-existent user");
        assertEquals(ErrorCode.USER_NOT_EXISTED, exception.getErrorCode(), "Error code should be USER_NOT_EXISTED");
    }

    @Test
    @DisplayName("Authenticate: Fail on Invalid Password")
    void authenticate_invalidPassword_throwsAppException() {
        // Arrange
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email(TEST_EMAIL)
                .password("wrongpassword")
                .build();

        // Act & Assert
        AppException exception = assertThrows(AppException.class,
                () -> authenticationService.authenticate(request),
                "Should throw AppException for incorrect password");
        assertEquals(ErrorCode.UNAUTHENTICATED, exception.getErrorCode(), "Error code should be UNAUTHENTICATED");
    }

    @Test
    @DisplayName("Validate Token: Success with Valid Token")
    void introspect_validToken_returnsValidResponse() throws Exception {
        // Arrange: First, authenticate to get a valid token
        AuthenticationRequest authRequest = AuthenticationRequest.builder()
                .email(TEST_EMAIL)
                .password(TEST_PASSWORD)
                .build();
        AuthenticationResponse authResponse = authenticationService.authenticate(authRequest);
        String validToken = authResponse.getToken();

        IntrospectRequest introspectRequest = IntrospectRequest.builder()
                .token(validToken)
                .build();

        // Act
        IntrospectResponse response = authenticationService.introspect(introspectRequest);

        // Assert
        assertNotNull(response, "Introspection response should not be null");
        assertTrue(response.isValid(), "Token should be considered valid");
    }

    @Test
    @DisplayName("Validate Token: Fail with Malformed Token")
    void introspect_invalidToken_returnsInvalidResponse() throws Exception {
        // Arrange
        IntrospectRequest introspectRequest = IntrospectRequest.builder()
                .token("this.is.an.invalid.token.format") // Malformed token string
                .build();

        // Act
        IntrospectResponse response = authenticationService.introspect(introspectRequest);

        // Assert
        assertNotNull(response, "Introspection response should not be null");
        assertFalse(response.isValid(), "Malformed token should be considered invalid");
    }

    @Test
    @DisplayName("Validate Token: Fail with Expired/Invalid Signature Token")
    void introspect_expiredToken_returnsInvalidResponse() throws Exception {
        // Arrange
        // Using a token string that is structurally JWT but likely has an invalid signature
        // or represents an expired token (for testing purposes).
        IntrospectRequest introspectRequest = IntrospectRequest.builder()
                .token("eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJleHBpcmVkIiwiZXhwIjoxNjA5NDU5MjAwfQ.signature")
                .build();


        // Act
        IntrospectResponse response = authenticationService.introspect(introspectRequest);

        // Assert
        assertNotNull(response);
        // Expecting verifyToken to fail due to signature check or expiry
        assertFalse(response.isValid(), "Expired or invalid signature token should be invalid");
    }


    @Test
    @DisplayName("Refresh Token: Success with Valid Token")
    void refreshToken_validToken_returnsNewToken() throws Exception {
        // Arrange: Authenticate to get a token
        AuthenticationRequest authRequest = AuthenticationRequest.builder()
                .email(TEST_EMAIL)
                .password(TEST_PASSWORD)
                .build();
        AuthenticationResponse authResponse = authenticationService.authenticate(authRequest);
        String originalToken = authResponse.getToken();

        // Ensure the original token isn't prematurely invalidated (sanity check)
        String originalJti = SignedJWT.parse(originalToken).getJWTClaimsSet().getJWTID();
        assertFalse(invalidatedTokenRepository.existsById(originalJti),
                "Original token should not be invalidated yet");

        RefreshRequest refreshRequest = RefreshRequest.builder()
                .token(originalToken)
                .build();

        // Act
        AuthenticationResponse refreshResponse = authenticationService.refreshToken(refreshRequest);

        // Assert
        assertNotNull(refreshResponse, "Refresh response should not be null");
        assertTrue(refreshResponse.isAuthenticated(), "Refresh response should indicate authentication");
        assertNotNull(refreshResponse.getToken(), "Refreshed token should not be null");
        assertThat(refreshResponse.getToken()).isNotBlank();
        assertThat(refreshResponse.getToken()).isNotEqualTo(originalToken); // Ensure a new token is generated

    }


    @Test
    @DisplayName("Refresh Token: Fail with Invalid/Malformed Token")
    void refreshToken_invalidToken_throwsAppException() {
        // Arrange
        RefreshRequest refreshRequest = RefreshRequest.builder()
                .token("invalid.token.for.refresh") // Malformed token string
                .build();

        // Act & Assert
        AppException exception = assertThrows(AppException.class,
                () -> authenticationService.refreshToken(refreshRequest),
                "Should throw AppException when refreshing with invalid/malformed token");
        // Expect UNAUTHENTICATED because parsing/verification fails
        assertEquals(ErrorCode.UNAUTHENTICATED, exception.getErrorCode(), "Error code should be UNAUTHENTICATED");
    }


    @Test
    @DisplayName("Logout: Success, Invalidates Valid Token")
    void logout_validToken_invalidatesToken() throws Exception {
        // Arrange: Authenticate to get a token
        AuthenticationRequest authRequest = AuthenticationRequest.builder()
                .email(TEST_EMAIL)
                .password(TEST_PASSWORD)
                .build();
        AuthenticationResponse authResponse = authenticationService.authenticate(authRequest);
        String tokenToLogout = authResponse.getToken();

        // Verify token is initially valid
        IntrospectRequest introspectRequestBefore = IntrospectRequest.builder().token(tokenToLogout).build();
        assertTrue(authenticationService.introspect(introspectRequestBefore).isValid(), "Token should be valid before logout");

        LogoutRequest logoutRequest = LogoutRequest.builder()
                .token(tokenToLogout)
                .build();

        // Act
        authenticationService.logout(logoutRequest);

        // Assert: Verify token is now invalid via introspection
        IntrospectRequest introspectRequestAfter = IntrospectRequest.builder().token(tokenToLogout).build();
        IntrospectResponse introspectResponse = authenticationService.introspect(introspectRequestAfter);
        assertFalse(introspectResponse.isValid(),"Token should be invalid after logout (introspection check)");

        // Also verify directly in the repository
        SignedJWT signedJWT = SignedJWT.parse(tokenToLogout);
        String jti = signedJWT.getJWTClaimsSet().getJWTID();
        assertTrue(invalidatedTokenRepository.existsById(jti), "Token ID should exist in invalidated repository after logout");
    }

    @Test
    @DisplayName("Logout: Graceful Handling of Invalid/Expired Token")
    void logout_invalidOrExpiredToken_doesNotThrowError() {
        // Arrange
        String invalidToken = "this.is.invalid"; // Malformed token string
        LogoutRequest logoutRequest = LogoutRequest.builder()
                .token(invalidToken)
                .build();

        // Act & Assert
        // Expect logout to complete without throwing, as errors are handled internally
        assertDoesNotThrow(() -> authenticationService.logout(logoutRequest),
                "Logout should handle invalid/malformed tokens gracefully without throwing");
    }
}