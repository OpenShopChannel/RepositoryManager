<#import "base.ftl" as base>

<@base.content>
    <div class="content">
        <#list messages as msg, class>
            <div class="alert alert-${class}">
                ${msg}
            </div>
            <br>
        </#list>

        <h1 class="content-title font-size-22"> <!-- font-size-22 = font-size: 2.2rem (22px) -->
            Users
        </h1>
        <table class="table table-bordered table-hover table-striped">
            <thead>
            <tr>
                <th></th>
                <th>ID</th>
                <th>Username</th>
                <th>Email Address</th>
                <th>Role</th>
                <th>Active</th>
                <th>Actions</th>
            </tr>
            </thead>
            <tbody>
            <#list users as user>
                <tr>
                    <th style="text-align: center"><i class="fa-solid fa-user"></i></th>
                    <td>${user.getId()}</td>
                    <td>${user.getUsername()}</td>
                    <td>${user.getEmail()}</td>
                    <td>${user.getRole().getDisplayName()}</td>
                    <td>${user.isEnabled()?string("Yes", "No")}</td>
                    <td><a href="/admin/users/view/${user.getId()}" class="btn" type="button">Modify</a></td>
                </tr>
            </#list>
            </tbody>
            <tfoot>
                <tr>
                    <td scope="row" colspan="6">
                        Total Users: ${users?size}
                    </td>
                    <th scope="row">
                        <a type="button" class="btn btn-success" href="/admin/users/new">New User</a>
                    </th>
                </tr>
            </tfoot>
        </table>
    </div>
</@base.content>