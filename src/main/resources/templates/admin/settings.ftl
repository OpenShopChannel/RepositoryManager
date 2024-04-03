<#import "base.ftl" as base>

<#macro setting title key>
    <div class="form-group">
        <label for="${key}">${title}</label>
        <input type="text" class="form-control" id="${key}" name="${key}" value="TODO">
    </div>
</#macro>

<@base.content>
    <div class="content">
        <h1 class="content-title font-size-22">
            Settings
        </h1>
        <div class="content-body">
            <form action="/admin/settings" method="post" enctype="multipart/form-data">
                <@setting title="Repository URL" key="git_url"/>
                <button type="submit" class="btn btn-primary">Save</button>
            </form>
        </div>
    </div>
</@base.content>