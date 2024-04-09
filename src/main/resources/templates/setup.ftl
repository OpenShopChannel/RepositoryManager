<html>
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Welcome to Repository Manager!</title>
    <meta name="robots" content="noindex">
    <link rel="stylesheet" href="/static/bulma/css/bulma.min.css">
    <link rel="stylesheet" href="/static/bulma-divider/dist/bulma-divider.min.css">
</head>
<body>
<section class="section">
    <div class="container">
        <#list messages as msg, class>
            <div class="notification is-${class}">
                ${msg}
            </div>
        </#list>
        <div class="box">
            <form method="post" action="">
                <h1 class="title has-text-centered">
                    Welcome to Open Shop Channel's Repository Manager! <small>${version}</small>
                </h1>
                <h2 class="subtitle has-text-centered">First time set-up</h2>

                <div class="divider">Repository Configuration</div>
                <!-- Git url -->
                <div class="field">
                    <label class="label" for="gitUrl">Git Repository</label>
                    <div class="control">
                        <input id="gitUrl" name="gitUrl" type="text" class="input" required>
                    </div>
                </div>

                <!-- Administrator user -->
                <div class="divider">Initial Admin User</div>

                <div class="field">
                    <label class="label" for="username">Username</label>
                    <div class="control">
                        <input id="admin.username" name="admin.username" type="text" class="input" required>
                    </div>
                </div>

                <div class="field">
                    <label class="label" for="email">Email</label>
                    <div class="control">
                        <input id="admin.email" name="admin.email" type="email" class="input" required>
                    </div>
                </div>

                <div class="field">
                    <label class="label" for="password">Password</label>
                    <div class="control">
                        <input id="admin.password" name="admin.password" type="password" class="input" required>
                    </div>
                </div>

                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>

                <!-- Submit Button -->
                <div class="field">
                    <div class="control">
                        <button id="submit" name="submit" type="submit" class="button is-link">Complete Setup</button>
                    </div>
                </div>
            </form>
        </div>
    </div>
</section>
</body>
</html>
