package com.group4.services;

import com.group4.models.Issue;
import com.group4.models.IssueStatus;
import com.group4.utils.FileConstants;
import com.group4.utils.FileHandler;
import com.group4.utils.SessionManager;
import javafx.concurrent.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service for issue management operations.
 */
public class IssueService {

    /**
     * Gets all issues.
     * 
     * @return A Task that returns a list of all issues
     */
    public Task<List<Issue>> getAllIssues() {
        return new Task<List<Issue>>() {
            @Override
            protected List<Issue> call() throws Exception {
                List<String> lines = FileHandler.readLines(FileConstants.ISSUES_FILE);
                List<Issue> issues = new ArrayList<>();

                for (String line : lines) {
                    Issue issue = Issue.fromDelimitedString(line);
                    if (issue != null) {
                        issues.add(issue);
                    }
                }

                return issues;
            }
        };
    }

    /**
     * Gets issues for the current user.
     * 
     * @return A Task that returns a list of issues for the current user
     */
    public Task<List<Issue>> getMyIssues() {
        return new Task<List<Issue>>() {
            @Override
            protected List<Issue> call() throws Exception {
                String userId = SessionManager.getInstance().getCurrentUser().getUserId();
                List<String> lines = FileHandler.readLines(FileConstants.ISSUES_FILE);
                List<Issue> issues = new ArrayList<>();

                for (String line : lines) {
                    Issue issue = Issue.fromDelimitedString(line);
                    if (issue != null && issue.getCustomerId().equals(userId)) {
                        issues.add(issue);
                    }
                }

                return issues;
            }
        };
    }

    /**
     * Gets issues assigned to the current staff member.
     * 
     * @return A Task that returns a list of issues assigned to the current staff
     *         member
     */
    public Task<List<Issue>> getAssignedIssues() {
        return new Task<List<Issue>>() {
            @Override
            protected List<Issue> call() throws Exception {
                String staffId = SessionManager.getInstance().getCurrentUser().getUserId();
                List<String> lines = FileHandler.readLines(FileConstants.ISSUES_FILE);
                List<Issue> issues = new ArrayList<>();

                for (String line : lines) {
                    Issue issue = Issue.fromDelimitedString(line);
                    if (issue != null && issue.getAssignedStaffId() != null &&
                            issue.getAssignedStaffId().equals(staffId)) {
                        issues.add(issue);
                    }
                }

                return issues;
            }
        };
    }

    /**
     * Gets an issue by its ID.
     * 
     * @param issueId The ID of the issue to get
     * @return A Task that returns the issue if found, null otherwise
     */
    public Task<Issue> getIssueById(String issueId) {
        return new Task<Issue>() {
            @Override
            protected Issue call() throws Exception {
                List<String> lines = FileHandler.readLines(FileConstants.ISSUES_FILE);

                for (String line : lines) {
                    Issue issue = Issue.fromDelimitedString(line);
                    if (issue != null && issue.getIssueId().equals(issueId)) {
                        return issue;
                    }
                }

                return null;
            }
        };
    }

    /**
     * Creates a new issue.
     * 
     * @param hallId      The ID of the hall
     * @param description The description of the issue
     * @return A Task that returns the created issue if successful, null otherwise
     */
    public Task<Issue> createIssue(String hallId, String description) {
        return new Task<Issue>() {
            @Override
            protected Issue call() throws Exception {
                // Generate a unique issue ID
                String issueId = "ISSUE-" + UUID.randomUUID().toString().substring(0, 8);

                // Get the current user ID
                String customerId = SessionManager.getInstance().getCurrentUser().getUserId();

                // Create a new issue
                Issue issue = new Issue(issueId, customerId, hallId, description, null, IssueStatus.OPEN);

                // Save the issue to the file
                FileHandler.appendLine(FileConstants.ISSUES_FILE, issue.toDelimitedString());

                return issue;
            }
        };
    }

    /**
     * Updates an issue.
     * 
     * @param issue The issue to update
     * @return A Task that returns true if successful, false otherwise
     */
    public Task<Boolean> updateIssue(Issue issue) {
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                List<String> lines = FileHandler.readLines(FileConstants.ISSUES_FILE);
                List<String> updatedLines = new ArrayList<>();
                boolean found = false;

                for (String line : lines) {
                    Issue existingIssue = Issue.fromDelimitedString(line);
                    if (existingIssue != null && existingIssue.getIssueId().equals(issue.getIssueId())) {
                        updatedLines.add(issue.toDelimitedString());
                        found = true;
                    } else {
                        updatedLines.add(line);
                    }
                }

                if (found) {
                    FileHandler.writeLines(FileConstants.ISSUES_FILE, updatedLines);
                    return true;
                }

                return false;
            }
        };
    }

    /**
     * Assigns an issue to a staff member.
     * 
     * @param issueId The ID of the issue
     * @param staffId The ID of the staff member
     * @return A Task that returns true if successful, false otherwise
     */
    public Task<Boolean> assignIssue(String issueId, String staffId) {
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                List<String> lines = FileHandler.readLines(FileConstants.ISSUES_FILE);
                List<String> updatedLines = new ArrayList<>();
                boolean found = false;

                for (String line : lines) {
                    Issue issue = Issue.fromDelimitedString(line);
                    if (issue != null) {
                        if (issue.getIssueId().equals(issueId)) {
                            issue.setAssignedStaffId(staffId);
                            issue.setStatus(IssueStatus.IN_PROGRESS);
                            updatedLines.add(issue.toDelimitedString());
                            found = true;
                        } else {
                            updatedLines.add(line);
                        }
                    }
                }

                if (found) {
                    FileHandler.writeLines(FileConstants.ISSUES_FILE, updatedLines);
                    return true;
                }

                return false;
            }
        };
    }

    /**
     * Updates the status of an issue.
     * 
     * @param issueId The ID of the issue
     * @param status  The new status
     * @return A Task that returns true if successful, false otherwise
     */
    public Task<Boolean> updateIssueStatus(String issueId, IssueStatus status) {
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                List<String> lines = FileHandler.readLines(FileConstants.ISSUES_FILE);
                List<String> updatedLines = new ArrayList<>();
                boolean found = false;

                for (String line : lines) {
                    Issue issue = Issue.fromDelimitedString(line);
                    if (issue != null) {
                        if (issue.getIssueId().equals(issueId)) {
                            issue.setStatus(status);
                            updatedLines.add(issue.toDelimitedString());
                            found = true;
                        } else {
                            updatedLines.add(line);
                        }
                    }
                }

                if (found) {
                    FileHandler.writeLines(FileConstants.ISSUES_FILE, updatedLines);
                    return true;
                }

                return false;
            }
        };
    }

    /**
     * Gets issues for a specific hall.
     * 
     * @param hallId The ID of the hall
     * @return A Task that returns a list of issues for the specified hall
     */
    public Task<List<Issue>> getIssuesByHallId(String hallId) {
        return new Task<List<Issue>>() {
            @Override
            protected List<Issue> call() throws Exception {
                List<String> lines = FileHandler.readLines(FileConstants.ISSUES_FILE);
                List<Issue> issues = new ArrayList<>();

                for (String line : lines) {
                    Issue issue = Issue.fromDelimitedString(line);
                    if (issue != null && issue.getHallId().equals(hallId)) {
                        issues.add(issue);
                    }
                }

                return issues;
            }
        };
    }

    /**
     * Gets issues by status.
     * 
     * @param status The status to filter by
     * @return A Task that returns a list of issues with the specified status
     */
    public Task<List<Issue>> getIssuesByStatus(IssueStatus status) {
        return new Task<List<Issue>>() {
            @Override
            protected List<Issue> call() throws Exception {
                List<String> lines = FileHandler.readLines(FileConstants.ISSUES_FILE);
                List<Issue> issues = new ArrayList<>();

                for (String line : lines) {
                    Issue issue = Issue.fromDelimitedString(line);
                    if (issue != null && issue.getStatus() == status) {
                        issues.add(issue);
                    }
                }

                return issues;
            }
        };
    }
}