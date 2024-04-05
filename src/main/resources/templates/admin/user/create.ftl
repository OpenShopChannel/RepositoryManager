<#import "../base.ftl" as base>

<@base.content>
    <div class="content">
        <#list messages as msg, class>
            <div class="alert alert-${class}">
                ${msg}
            </div>
            <br>
        </#list>

        <h1 class="content-title font-size-22"> <!-- font-size-22 = font-size: 2.2rem (22px) -->
            Create new user
        </h1>

        <div class="content-body">
            <form action="/admin/users/new" method="post" enctype="multipart/form-data">
                <div class="form-group">
                    <label for="username" class="required">Username</label>
                    <input type="text" class="form-control" id="username" name="username" required>
                </div>
                <div class="form-group">
                    <label for="email" class="required">Email</label>
                    <input type="email" class="form-control" id="email" name="email" required>
                </div>
                <div class="form-group">
                    <label for="role" class="required">Role</label>
                    <select class="form-control" id="role" name="role" required>
                        <option value="administrator">Administrator</option>
                        <option value="moderator">Moderator</option>
                        <option value="guest">Guest</option>
                    </select>
                </div>

                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>

                <!-- Submit Button -->
                <button type="submit" class="btn btn-primary">Create User</button>
            </form>
        </div>
    </div>
</@base.content>