<html>
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Change Password</title>
    <meta name="robots" content="noindex">
    <link rel="stylesheet" href="/static/bulma/css/bulma.min.css">
</head>
<body>
<section class="section">
    <div class="container">
        <h1 class="title">
            Change Password
        </h1>
        <form class="form-horizontal" action="" method="post">
            <fieldset>
                <!-- Password -->
                <div class="field">
                    <label class="label" for="password">New Password</label>
                    <div class="control">
                        <input id="password" name="password" type="password" class="input" required>
                    </div>
                </div>

                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>

                <!-- Submit Button -->
                <div class="field">
                    <div class="control">
                        <button id="submit" name="submit" type="submit" class="button is-link">Change Password</button>
                    </div>
                </div>
            </fieldset>
        </form>
    </div>
</section>
</body>
</html>
