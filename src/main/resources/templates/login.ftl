<html>
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Admin Login</title>
    <meta name="robots" content="noindex">
    <link rel="stylesheet" href="/static/bulma/css/bulma.min.css">
</head>
<body>
<section class="section">
    <div class="container">
        <#list messages as message>
            <#list message as msg, class>
                <div class="notification is-${class}">
                    ${msg}
                </div>
            </#list>
        </#list>
        <h1 class="title">
            Administrator Login
        </h1>
        <form class="form-horizontal" action="" method="post">
            <fieldset>
                <!-- Username -->
                <div class="field">
                    <label class="label" for="username">Username</label>
                    <div class="control">
                        <input id="username" name="username" type="text" class="input">
                    </div>
                </div>

                <!-- Password -->
                <div class="field">
                    <label class="label" for="password">Password</label>
                    <div class="control">
                        <input id="password" name="password" type="password" class="input">
                    </div>
                </div>

                <!-- Remember Me -->
                <div class="field">
                    <label class="checkbox">
                        <input type="checkbox" name="remember-me">
                        Remember me
                    </label>
                </div>

                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>

                <!-- Submit Button -->
                <div class="field">
                    <div class="control">
                        <button id="loginbutton" name="loginbutton" type="submit" class="button is-link">Login</button>
                    </div>
                </div>
            </fieldset>
        </form>
    </div>
</section>
</body>
</html>
