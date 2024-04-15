<#import "base.ftl" as base>

<@base.content>
    <div class="content">
        <div class="row row-eq-spacing">
            <div class="col-4">
            <img src="/static/assets/images/logo-text.png" class="img-fluid" alt="Repository Manager Logo">
            </div>
        </div>
        <h1 class="content-title font-size-22">
            Repository Administration Panel
        </h1>
        You are logged in as <strong>${currentUser.getUsername()}</strong>.
    </div>
    <#if currentUser.hasAccess("Administrator")>
    <div class="card">
        <div class="row row-eq-spacing justify-content-center" style="margin-top: unset; margin-bottom: unset;">
            <div class="btn-group btn-group-lg" role="group" aria-label="Basic example">
                <a href="/admin/action/update" class="btn btn-success" type="button">Update</a>
                <a href="/admin/settings" class="btn" type="button">Settings</a>
            </div>
        </div>
    </div>
    </#if>
    <div class="card">
        <h2 class="card-title">
            ${repoInfo.name()} (Provider: ${repoInfo.provider()})
        </h2>
        <p>
            ${repoInfo.description()}
        </p>
    </div>
    <div class="row row-eq-spacing">
        <div class="col-6 col-xl-3">
            <div class="card">
                <h2 class="card-title">Applications</h2>
                ${applications}
            </div>
        </div>
        <div class="col-6 col-xl-3">
            <div class="card">
                <h2 class="card-title">Pending Moderation</h2>
                TODO <!--TODO notifications()["pending_moderation"]-->
            </div>
        </div>
        <div class="col-6 col-xl-3">
            <div class="card">
                <h2 class="card-title">Downloads Today</h2>
                0
            </div>
        </div>
    </div>
</@base.content>