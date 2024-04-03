<#import "base.ftl" as base>
<#assign FormatUtil=statics['org.oscwii.repositorymanager.utils.FormatUtil']>

<@base.content>
    <div class="content">
        <h1 class="content-title font-size-22"> <!-- font-size-22 = font-size: 2.2rem (22px) -->
            Applications
        </h1>
        <table class="table table-bordered table-hover table-striped">
            <thead>
            <tr>
                <th>File</th>
                <th>Name</th>
                <th>Author</th>
                <th>Category</th>
                <th>Generated Index Entry (Debug)</th>
                <th>Links</th>
            </tr>
            </thead>
            <tbody>
            <!-- TODO pagination -->
            <#list contents as app>
                <tr>
                    <th>${app.getSlug()}.oscmeta</th>
                    <th>${app.getMetaXml().name()}</th>
                    <td>${app.getMetaXml().coder()}</td>
                    <td>${app.getCategory()}</td>
                    <td>
                        <details>
                            <summary class="btn">App Information</summary>
                            <ul><br>${app.describe()}</ul>
                        </details>
                    </td>
                    <td><a href="${FormatUtil.zipUrl(app.getSlug())}" class="btn" type="button">Download</a></td>
                </tr>
            </#list>
            </tbody>
        </table>
    </div>
</@base.content>