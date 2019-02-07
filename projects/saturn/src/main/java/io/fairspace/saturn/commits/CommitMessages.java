package io.fairspace.saturn.commits;


import java.util.function.Supplier;

import static io.fairspace.saturn.Context.currentRequest;

/**
 * Manages commit messages.
 * A commit message can be set either externally using Saturn-Commit-Message header of the incoming HTTP request
 * or by calling withCommitMessage.
 */
public class CommitMessages {
    public static final String COMMIT_MESSAGE_HEADER = "Saturn-Commit-Message";

    private static final ThreadLocal<String> systemCommitMessage = new ThreadLocal<>();

    public static void withCommitMessage(String message, Runnable action) {
        systemCommitMessage.set(message);
        try {
            action.run();
        } finally {
            systemCommitMessage.set(null);
        }
    }

    public static <T> T withCommitMessage(String message, Supplier<T> action) {
        systemCommitMessage.set(message);
        try {
            return action.get();
        } finally {
            systemCommitMessage.set(null);
        }
    }

    public static String getCommitMessage() {
        var systemMessage = systemCommitMessage.get();
        if (systemMessage != null) {
           return systemMessage;
        }

        return currentRequest()
                .map(request -> request.getHeader(COMMIT_MESSAGE_HEADER))
                .orElse(null);
    }
}
