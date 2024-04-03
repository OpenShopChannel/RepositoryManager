<#import "base.ftl" as base>
<#assign FormatUtil=statics['org.oscwii.repositorymanager.utils.FormatUtil']>

<@base.content>
    <div class="content">
        <h1 class="content-title font-size-22"> <!-- font-size-22 = font-size: 2.2rem (22px) -->
            Logs
        </h1>
        <table class="table table-bordered table-hover table-striped">
            <thead>
            <tr>
                <th></th>
                <th>File</th>
                <th>Length</th>
                <th>Errors</th>
                <th>Creation Date</th>
                <th></th>
            </tr>
            </thead>
            <tbody>
            <#list logs as log>
                <tr>
                    <th style="text-align: center"><i class="fa-solid fa-file-lines"></i></th>
                    <th>${log.name()}</th>
                    <th>${log.lines()} Lines</th>
                    <th>${log.errors()}</th>
                    <td>${log.creationDate()}</td>
                    <td><a href="${FormatUtil.logUrl(log.name())}" class="btn" type="button">Download</a></td>
                </tr>
            </#list>
            </tbody>
        </table>
    </div>
</@base.content>