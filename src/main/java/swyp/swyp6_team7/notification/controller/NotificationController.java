package swyp.swyp6_team7.notification.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import swyp.swyp6_team7.global.utils.auth.RequireUserNumber;
import swyp.swyp6_team7.notification.dto.NotificationDto;
import swyp.swyp6_team7.notification.service.NotificationService;

@RequiredArgsConstructor
@RestController
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);
    private final NotificationService notificationService;

    @GetMapping("/api/notifications")
    public ResponseEntity<Page<NotificationDto>> getNotificationsByUser(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequireUserNumber Integer userNumber
    ) {
        logger.info("Notifications 조회 요청 - userId: {}", userNumber);

        Page<NotificationDto> notifications = notificationService.getNotificationsByUser(PageRequest.of(page, size), userNumber);
        return ResponseEntity.status(HttpStatus.OK)
                .body(notifications);
    }

}
