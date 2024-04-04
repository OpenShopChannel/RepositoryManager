<#macro navbar>
    <nav class="navbar">
        <!-- Navbar content (with toggle sidebar button) -->
        <div class="navbar-content">
            <button class="btn btn-action" type="button" onclick="halfmoon.toggleSidebar()">
                <i class="fa fa-bars" aria-hidden="true"></i>
                <span class="sr-only">Toggle sidebar</span> <!-- sr-only = show only on screen readers -->
            </button>
        </div>
        <!-- Navbar brand -->
        <a href="/admin" class="navbar-brand">
            <img src="/static/assets/images/logo.png" alt="Repository Manager">
            Repository Manager
        </a>
        <!-- Navbar text -->
        <span class="navbar-text text-monospace">by Open Shop Channel</span>
        <!-- Navbar content (with the dropdown menu) -->
        <div class="navbar-content d-md-none ml-auto">
            <div class="dropdown with-arrow">
                <button class="btn" data-toggle="dropdown" type="button" id="navbar-dropdown-toggle-btn-1">
                    Menu
                    <i class="fa fa-angle-down" aria-hidden="true"></i>
                </button>
                <div class="dropdown-menu dropdown-menu-right w-200" aria-labelledby="navbar-dropdown-toggle-btn-1">
                    <div class="dropdown-divider"></div>
                    <div class="dropdown-content">
                        Logged in as ${currentUser.getEmail()}
                    </div>
                </div>
            </div>
        </div>
    </nav>
</#macro>