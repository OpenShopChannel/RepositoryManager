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
            Modify User: ${user.getUsername()}
        </h1>

        <div class="modal" id="reset-pw-modal" tabindex="-1" role="dialog">
            <div class="modal-dialog" role="document">
                <div class="modal-content">
                    <h5 class="modal-title">Confirm Action</h5>
                    <p>
                        Are you sure you want to do this?
                        <br>This user will be emailed a link to reset their password.
                        <br>
                    </p>

                    <div class="text-right mt-20">
                        <br>
                        <form action="/admin/users/reset-password/${user.getId()}" method="post" class="w-400 mw-full">
                            <a href="#" type="button" class="btn btn-primary" data-dismiss="modal">Cancel</a>
                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                            <button type="submit" class="btn btn-danger">Send reset password link</button>
                        </form>
                    </div>
                </div>
            </div>
        </div>

        <div class="modal" id="delete-modal" tabindex="-1" role="dialog">
            <div class="modal-dialog" role="document">
                <div class="modal-content">
                    <h5 class="modal-title">Confirm Action</h5>
                    <p>
                        Are you sure you want to do this?
                        <br>This action cannot be undone.
                        <br>
                    </p>

                    <div class="text-right mt-20">
                        <br>
                        <form action="/admin/users/delete/${user.getId()}" method="post" class="w-400 mw-full">
                            <a href="#" type="button" class="btn btn-primary" data-dismiss="modal">Cancel</a>
                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                            <#if currentUser.getId() == user.getId()>
                                <button type="submit" class="btn btn-danger" disabled>You cannot delete yourself.</button>
                            <#else>
                                <button type="submit" class="btn btn-danger">Delete User</button>
                            </#if>
                        </form>
                    </div>
                </div>
            </div>
        </div>

        <div class="content-body">
            <div class="row">
                <!-- Identity column -->
                <div class="col-sm-6">
                    <div class="card">
                        <h2 class="card-title">Identity</h2>
                        <form action="/admin/users/view/${user.getId()}" method="post" class="w-400 mw-full">
                            <div class="form-group">
                                <label for="id">ID</label>
                                <input type="text" class="form-control" id="id" name="id" value="${user.getId()}" readonly required>
                            </div>
                            <div class="form-group">
                                <label for="username">Username</label>
                                <input type="text" class="form-control" id="username" name="username" value="${user.getUsername()}" readonly required>
                            </div>
                            <div class="form-group">
                                <label for="email" class="required">Email</label>
                                <input type="email" class="form-control" id="email" name="email" value="${user.getEmail()}" required>
                            </div>
                            <div class="form-group">
                                <div class="custom-checkbox">
                                    <input type="checkbox" id="enabled" name="enabled" ${user.isEnabled()?string("checked", "")}
                                        ${(currentUser.getId() == user.getId())?string("disabled", "")}>
                                    <label for="enabled" class="checkbox">Enable Account</label>
                                </div>
                            </div>

                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>

                            <!-- Submit Button -->
                            <button type="submit" class="btn btn-primary">Update User</button>
                        </form>
                    </div>
                </div>

                <!-- Role/Delete column -->
                <div class="col-sm-6">
                    <div class="card">
                        <h2 class="card-title">Role</h2>
                        <form action="/admin/users/view/${user.getId()}" method="post" class="w-400 mw-full">
                            <div class="form-group">
                                <label for="role">Role</label>
                                <select class="form-control" id="role" name="role" required>
                                    <option value="" disabled selected>Select a role</option>
                                    <option value="administrator">Administrator</option>
                                    <option value="moderator">Moderator</option>
                                    <option value="guest">Guest</option>
                                </select>
                            </div>

                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>

                            <!-- Submit Button -->
                            <button type="submit" class="btn btn-primary">Update Role</button>
                        </form>
                    </div>

                    <div class="card">
                        <h2 class="card-title">Danger Zone</h2>
                        <a href="#reset-pw-modal" class="btn" role="button">Reset Password</a>
                        <a href="#delete-modal" class="btn btn-danger" role="button">Delete User</a>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script src="/static/halfmoon/js/halfmoon.min.js"></script>
</@base.content>