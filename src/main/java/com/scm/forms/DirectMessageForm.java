package com.scm.forms;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class DirectMessageForm {

    @NotBlank(message = "Recipient contact selection is required")
    private String recipientEmail;

    @NotBlank(message = "Subject is required")
    @Size(min = 3, max = 200, message = "Subject must be between 3 and 200 characters")
    private String subject;

    @NotBlank(message = "Message body is required")
    @Size(min = 10, max = 4000, message = "Message body must be between 10 and 4000 characters")
    private String body;
}
