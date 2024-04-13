<#import "base.ftl" as base>

<#macro table entries status>
    <table class="table table-bordered table-hover table-striped">
        <thead>
        <tr>
            <th></th>
            <th>Binary ID</th>
            <th>Discovered</th>
            <th>Mod Actions</th>
            <th>Links</th>
        </tr>
        </thead>
        <tbody>
        <!-- TODO pagination -->
        <#list entries as mod_entry>
            <#if mod_entry.status() == status>
                <tr>
                    <th style="text-align: center"><i class="fa-solid fa-gavel"></i></th>
                    <th>${mod_entry.app()}-${mod_entry.checksum()?truncate_c(24, "...")}</th>
                    <th>${mod_entry.discoveryDate()}</th>
                    <td>
                        <div class="btn-group" role="group">
                            <#if mod_entry.status() != "APPROVED">
                                <a href="/admin/moderation/${mod_entry.checksum()}/approve" class="btn btn-success" type="button">Approve</a>
                            </#if>
                            <#if mod_entry.status() != "REJECTED">
                                <a href="/admin/moderation/${mod_entry.checksum()}/reject" class="btn btn-danger" type="button">Reject</a>
                            </#if>
                        </div>
                    </td>
                    <td><a href="/admin/moderation/${mod_entry.checksum()}/download" class="btn" type="button">Download .ZIP</a></td>
                </tr>
            </#if>
        </#list>
        </tbody>
    </table>
</#macro>

<@base.content>
    <div class="content">
        <h1 class="content-title font-size-22">
            Moderation
        </h1>

        <div class="collapse-group">
            <details class="collapse-panel" open>
                <summary class="collapse-header">
                    Pending
                </summary>
                <div class="collapse-content">
                    <@table modEntries "PENDING"/>
                </div>
            </details>

            <details class="collapse-panel">
                <summary class="collapse-header">
                    Approved
                </summary>
                <div class="collapse-content">
                    <@table modEntries "APPROVED"/>
                </div>
            </details>

            <details class="collapse-panel">
                <summary class="collapse-header">
                    Rejected
                </summary>
                <div class="collapse-content">
                    <@table modEntries "REJECTED"/>
                </div>
            </details>
        </div>
    </div>
</@base.content>