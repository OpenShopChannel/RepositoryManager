<html>
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Admin Sign Up</title>
    <meta name="robots" content="noindex">
    <link rel="stylesheet" href="/static/bulma/css/bulma.min.css">
</head>
<body>
<section class="section">
    <div class="container">
        <#list messages as msg, class>
            <div class="notification is-${class}">
                ${msg}
            </div>
        </#list>
        <h1 class="title">
            Administrator Sign Up
        </h1>
        <form class="form-horizontal" action="" method="post">
            <fieldset>
                <!-- Email -->
                <div class="field">
                    <label class="label" for="email">Email</label>
                    <div class="control">
                        <input id="email" name="email" type="email" class="input">
                    </div>
                </div>

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

                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>

                <!-- Submit Button -->
                <div class="field">
                    <div class="control">
                        <button id="loginbutton" name="loginbutton" type="submit" class="button is-link">Sign Up</button>
                    </div>
                </div>
            </fieldset>
        </form>
    </div>
</section>
</body>
</html>
