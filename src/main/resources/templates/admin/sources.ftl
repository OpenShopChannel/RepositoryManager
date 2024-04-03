<#import "base.ftl" as base>

<@base.content>
    <div class="content">
        <h1 class="content-title font-size-22">
            Available Sources
        </h1>
        <table class="table table-bordered table-hover table-striped">
            <thead>
            <tr>
                <th></th>
                <th>ID</th>
                <th>Name</th>
                <th>Description</th>
            </tr>
            </thead>
            <tbody>
            <#list sources as source>
                <tr>
                    <th style="text-align: center"><i class="fa-solid fa-cloud"></i></th>
                    <th>${source.getType()}</th>
                    <th>${source.getName()}</th>
                    <th>${source.getDescription()}</th>
                </tr>
            </#list>
            </tbody>
        </table>
    </div>
</@base.content>