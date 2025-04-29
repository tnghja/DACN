//package com.ecommerce.inventory.service;
//
//
//import com.ecommerce.inventory.model.entity.User;
//import com.recombee.api_client.RecombeeClient;
//import com.recombee.api_client.api_requests.Batch;
//import com.recombee.api_client.api_requests.Request;
//import com.recombee.api_client.api_requests.SetUserValues;
//import com.recombee.api_client.exceptions.ApiException;
//import org.springframework.stereotype.Service;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//public class RecombeeSyncService {
//    private final RecombeeClient recombeeClient;
//
//    public RecombeeSyncService(RecombeeClient recombeeClient) {
//        this.recombeeClient = recombeeClient;
//    }
//
//
//
//    public void syncUser(User user) {
//        try {
//            recombeeClient.send(new SetUserValues(
//                    String.format("user-%s", user.getUserId()),
//                    new HashMap<String, Object>() {{
//                        put("name", user.getUserName());
//                        put("email", user.getEmail());
//                        put("phoneNumber", user.getPhoneNumber());
//                        put("avatarUrl", user.getAvtUrl());
//                    }}).setCascadeCreate(true));
//        } catch (ApiException e) {
//            e.printStackTrace();
//            // Handle the exception, e.g., log or use fallback
//        }
//    }
//
//    // Đồng bộ danh sách user lên Recombee (batch)
//    public void batchSyncUsers(List<User> users) {
//        try {
//            List<Request> requests = users.stream()
//                    .map(user -> new SetUserValues(
//                            String.format("user-%s", user.getUserId()),
//                            new HashMap<String, Object>() {{
//                                put("name", user.getUserName());
//                                put("email", user.getEmail());
//                                put("phoneNumber", user.getPhoneNumber());
//                                put("avatarUrl", user.getAvtUrl());
//                            }}).setCascadeCreate(true)).collect(Collectors.toList());
//
//
//            recombeeClient.send(new Batch(requests));
//        } catch (ApiException e) {
//            e.printStackTrace();
//            // Handle the exception, e.g., log or use fallback
//        }
//    }
//}
