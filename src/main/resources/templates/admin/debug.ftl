<#import "base.ftl" as base>

<@base.content>
    <div class="content">
        <h1 class="content-title font-size-22"> <!-- font-size-22 = font-size: 2.2rem (22px) -->
            Debug
        </h1>
        <div class="row">
            <a href="debug/init_repo" class="btn btn-danger" role="button">Initialize Repository</a>
            <a href="debug/pull_repo" class="btn" role="button">Pull Remote</a>
            <a href="debug/update_index" class="btn" role="button">Update Index</a>
            <a href="debug/reset_index" class="btn" role="button">Reset Index</a>
        </div>
    </div>
</@base.content>