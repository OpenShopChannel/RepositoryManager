<#import "includes/header.ftl" as header>
<#import "includes/navbar.ftl" as navbar>
<#import "includes/sidebar.ftl" as sidebar>

<#macro content>
    <@header.header/>

    <body class="with-custom-webkit-scrollbars with-custom-css-scrollbars" data-dm-shortcut-enabled="true"
          data-sidebar-shortcut-enabled="true" data-set-preferred-mode-onload="true">

    <div class="page-wrapper with-navbar with-sidebar" data-sidebar-type="overlayed-sm-and-down">

        <div class="sticky-alerts"></div>

        <@navbar.navbar/>

        <div class="sidebar-overlay" onclick="halfmoon.toggleSidebar()"></div>
        <@sidebar.sidebar/>

        <!-- Content wrapper start -->
        <div class="content-wrapper">
            <!-- Page wrapper with content-wrapper inside -->
            <div class="page-wrapper">
                <div class="content-wrapper">
                    <!-- Container-fluid -->
                    <div class="container-fluid">
                        <#nested>
                    </div>
                </div>
            </div>
        </div>
        <!-- Content wrapper end -->

    </div>
    <!-- Page wrapper end -->

    <!-- Halfmoon JS -->
    <script src="/static/halfmoon/js/halfmoon.min.js"></script>
    </body>
    </html>
</#macro>
