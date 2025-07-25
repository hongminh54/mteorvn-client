/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorvnclient.gui.screens;

import meteordevelopment.meteorvnclient.addons.GithubRepo;
import meteordevelopment.meteorvnclient.addons.MeteorAddon;
import meteordevelopment.meteorvnclient.gui.GuiTheme;
import meteordevelopment.meteorvnclient.gui.WindowScreen;
import meteordevelopment.meteorvnclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorvnclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorvnclient.utils.misc.TranslationUtils;
import meteordevelopment.meteorvnclient.utils.network.Http;
import meteordevelopment.meteorvnclient.utils.network.MeteorExecutor;
import net.minecraft.util.Util;

import java.net.http.HttpResponse;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class CommitsScreen extends WindowScreen {
    private final MeteorAddon addon;
    private Commit[] commits;
    private int statusCode;

    public CommitsScreen(GuiTheme theme, MeteorAddon addon) {
        super(theme, "Commits for " + addon.name);

        this.addon = addon;

        locked = true;
        lockedAllowClose = true;

        MeteorExecutor.execute(() -> {
            GithubRepo repo = addon.getRepo();

            if (addon.getCommit() == null || addon.getCommit().equals("${commit}")) {
                statusCode = 404;
                taskAfterRender = this::populateError;
                return;
            }

            Http.Request request = Http.get(String.format("https://api.github.com/repos/%s/compare/%s...%s", repo.getOwnerName(), addon.getCommit(), repo.branch()));
            repo.authenticate(request);
            HttpResponse<Response> res = request.sendJsonResponse(Response.class);

            if (res.statusCode() == Http.SUCCESS) {
                commits = res.body().commits;
                taskAfterRender = this::populateCommits;
            } else {
                statusCode = res.statusCode();
                taskAfterRender = this::populateError;
            }
        });
    }

    @Override
    public void initWidgets() {
        // Only initialize widgets after data arrives
    }

    private void populateHeader(String headerMessage) {
        WHorizontalList l = add(theme.horizontalList()).expandX().widget();

        l.add(theme.label(headerMessage)).expandX();

        String website = addon.getWebsite();
        if (website != null) l.add(theme.button("Website")).widget().action = () -> Util.getOperatingSystem().open(website);

        l.add(theme.button("GitHub")).widget().action = () -> {
            GithubRepo repo = addon.getRepo();
            Util.getOperatingSystem().open(String.format("https://github.com/%s/tree/%s", repo.getOwnerName(), repo.branch()));
        };
    }

    private void populateError() {
        String errorMessage = switch (statusCode) {
            case Http.BAD_REQUEST -> TranslationUtils.translate("gui.connection_dropped", "Connection dropped");
            case Http.UNAUTHORIZED -> TranslationUtils.translate("gui.unauthorized", "Unauthorized");
            case Http.FORBIDDEN -> TranslationUtils.translate("gui.rate_limited", "Rate-limited");
            case Http.NOT_FOUND -> TranslationUtils.translate("gui.invalid_commit_hash", "Invalid commit hash");
            default -> TranslationUtils.translate("gui.error_code", "Error Code: ") + statusCode;
        };

        populateHeader(TranslationUtils.translate("gui.error_fetching_commits", "There was an error fetching commits: ") + errorMessage);

        if (statusCode == Http.UNAUTHORIZED) {
            add(theme.horizontalSeparator()).padVertical(theme.scale(8)).expandX();
            WHorizontalList l = add(theme.horizontalList()).expandX().widget();

            l.add(theme.label(TranslationUtils.translate("gui.consider_auth_token", "Consider using an authentication token: "))).expandX();
            l.add(theme.button(TranslationUtils.translate("gui.authorization_guide", "Authorization Guide"))).widget().action = () -> {
                Util.getOperatingSystem().open("https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens");
            };
        }

        locked = false;
    }

    private void populateCommits() {
        // Top
        String text = TranslationUtils.translate("gui.new_commits_plural", "There are %d new commits");
        if (commits.length == 1) text = TranslationUtils.translate("gui.new_commits_singular", "There is %d new commit");
        populateHeader(String.format(text, commits.length));

        // Commits
        if (commits.length > 0) {
            add(theme.horizontalSeparator()).padVertical(theme.scale(8)).expandX();

            WTable t = add(theme.table()).expandX().widget();
            t.horizontalSpacing = 0;

            for (Commit commit : commits) {
                String date = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(commit.commit.committer.date));
                t.add(theme.label(date)).top().right().widget().color = theme.textSecondaryColor();

                t.add(theme.label(getMessage(commit))).widget().action = () -> Util.getOperatingSystem().open(String.format("https://github.com/%s/commit/%s", addon.getRepo().getOwnerName(), commit.sha));
                t.row();
            }
        }

        locked = false;
    }

    private static String getMessage(Commit commit) {
        StringBuilder sb = new StringBuilder(" - ");
        String message = commit.commit.message;

        for (int i = 0; i < message.length(); i++) {
            if (i >= 80) {
                sb.append("...");
                break;
            }

            char c = message.charAt(i);

            if (c == '\n') {
                sb.append("...");
                break;
            }

            sb.append(c);
        }

        return sb.toString();
    }

    private static class Response {
        public Commit[] commits;
    }

    private static class Commit {
        public String sha;
        public CommitInner commit;
    }

    private static class CommitInner {
        public Committer committer;
        public String message;
    }

    private static class Committer {
        public String date;
    }
}
