<#macro category title>
    <h5 class="sidebar-title">${title}</h5>
    <div class="sidebar-divider"></div>
</#macro>

<#macro page title icon path required_role="" pill=0 pill_plural="items">
    <#if currentUser.hasAccess(required_role)>
    <a href="${path}" class="sidebar-link sidebar-link-with-icon <#if request.getServletPath() == path>active</#if>">
        <span class="sidebar-icon"><i class="fa ${icon}" aria-hidden="true"></i></span>
        ${title}<#if pill gt 0><span class="badge badge-pill badge-danger" style="margin-left: 5px;">${pill} ${pill_plural}</span></#if>
    </a>
    </#if>
</#macro>

<#macro sidebar>
    <div class="sidebar">
        <div class="sidebar-menu">
            <a href="/admin" class="sidebar-brand">
                Admin
            </a>
            <div class="sidebar-content">
                <div class="mt-10 font-size-12">
                    Hello ${currentUser.getUsername()}.
                </div>
                <span class="badge badge-secondary">${currentUser.getRole().getDisplayName()}</span>
            </div>
            <@category title="General"/>
                <@page title="Home" icon="fa-home" path="/admin"/>
                <@page title="Debug" icon="fa-bug" path="/admin/debug" required_role="Administrator"/>
            <@category title="Moderation"/>
                <@page title="Moderation" icon="fa-gavel" path="/admin/moderation" required_role="Moderator" pill=0 pill_plural="pending"/> <!-- TODO -->
            <@category title="Repository"/>
                <@page title="Applications" icon="fa-rectangle-list" path="/admin/apps"/>
            <@category title="System"/>
                <@page title="Users" icon="fa-users" path="/admin/users" required_role="Administrator"/>
                <@page title="Sources" icon="fa-globe" path="/admin/sources" required_role="Moderator"/>
                <@page title="Log Files" icon="fa-folder-open" path="/admin/logs" required_role="Administrator"/>
                <@page title="Settings" icon="fa-cog" path="/admin/settings" required_role="Administrator"/>
        </div>
    </div>
</#macro>