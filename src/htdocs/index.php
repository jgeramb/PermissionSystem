<!DOCTYPE html>
<html lang="en">
  <head>
    <title>WebPerms</title>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="style.css">
  </head>
  <body>
      <div id="header" class="unselectable">
        <h1 onclick="window.location.href='?authKey=%auth_key%';">WebPerms</h1>
        <h2>by Justix</h2>
      </div>
      <div id="content">
        <div id="sidebar" class="unselectable">
          <div id="sidebarIconBackground">
            <svg id="sidebarIcon" width="24" height="24" viewBox="0 0 192 512" style="transform: rotateZ(180deg);" onclick="toggleSidebar()">
              <path fill="currentColor" d="M0 384.662V127.338c0-17.818 21.543-26.741 34.142-14.142l128.662 128.662c7.81 7.81 7.81 20.474 0 28.284L34.142 398.804C21.543 411.404 0 402.48 0 384.662z">
              </path>
            </svg>
          </div>
          <div id="sidebarItems" class="scrollable">
            <div class="sidebarItem">
              <div class="iconBackground">
                <svg id="groupIcon" width="24" height="24" viewBox="0 0 192 512" style="transform: rotateZ(90deg);" onclick="toggleGroups()">
                  <path fill="currentColor" d="M0 384.662V127.338c0-17.818 21.543-26.741 34.142-14.142l128.662 128.662c7.81 7.81 7.81 20.474 0 28.284L34.142 398.804C21.543 411.404 0 402.48 0 384.662z">
                  </path>
                </svg>
              </div>
              <h1>GROUPS (%group_amount%)</h1>
              <ul id="groups">
                %groups%
              </ul>
            </div>
            <div class="sidebarItem">
              <div class="iconBackground">
                <svg id="userIcon" width="24" height="24" viewBox="0 0 192 512" style="transform: rotateZ(90deg);" onclick="toggleUsers()">
                  <path fill="currentColor" d="M0 384.662V127.338c0-17.818 21.543-26.741 34.142-14.142l128.662 128.662c7.81 7.81 7.81 20.474 0 28.284L34.142 398.804C21.543 411.404 0 402.48 0 384.662z">
                  </path>
                </svg>
              </div>
              <h1>USERS (%user_amount%)</h1>
              <ul id="users">
                %users%
              </ul>
            </div>
          </div>
        </div>
        <div id="editor" class="scrollable">
          <div id="rows">
            %permissions%
          </div>
        </div>
        <form id="addPermissionForm" method="GET">
          <input type="hidden" name="authKey" value="%auth_key%">
          <input type="hidden" name="%current_type%" value="%current_name%">
          <input type="text" name="permission" value="" placeholder="Enter a permission..." required>
          <label class="checkbox unselectable">
            <input type="checkbox" name="value" checked>
            <span class="checkmark"></span>
          </label>
          <button type="submit">Add</button>
        </form>
      </div>
      <div id="footer" class="unselectable">
          <p>© 2020 JustixDevelopment</p>
      </div>

      <script type="text/javascript">
        const SIDEBAR = document.getElementById('sidebar'), USERS = document.getElementById('users'), GROUPS = document.getElementById('groups'), SIDEBAR_ICON = document.getElementById('sidebarIcon'), USER_ICON = document.getElementById('userIcon'), GROUP_ICON = document.getElementById('groupIcon');
        var sidebarOpen = true, usersOpen = true, groupsOpen = true;

        function toggleSidebar() {
          sidebarOpen = !(sidebarOpen);

          SIDEBAR.style.marginLeft = sidebarOpen ? '0px' : '-230px';
          SIDEBAR_ICON.style.transform = sidebarOpen ? 'rotateZ(180deg)' : '';
        }

        function toggleGroups() {
          groupsOpen = !(groupsOpen);

          GROUPS.style.display = groupsOpen ? 'block' : 'none';
          GROUP_ICON.style.transform = 'rotateZ(' + (groupsOpen ? '90' : '-90') + "deg)";
        }

        function toggleUsers() {
          usersOpen = !(usersOpen);

          USERS.style.display = usersOpen ? 'block' : 'none';
          USER_ICON.style.transform = 'rotateZ(' + (usersOpen ? '90' : '-90') + "deg)";
        }

        function removePermission(subElement) {
          let element = subElement.parentElement;
          element.classList.add('beingRemoved');
          element.style.transform = "translateX(" + (window.innerWidth * 0.75) + "px)";
          element.style.opacity = 0;
          element.style.backgroundColor = "#AA0000";

          for (let childElement of element.children)
            childElement.classList.add('beingRemoved');

          setTimeout(function() {
            window.location.href += '&action=removePerm&value=' + ((element.children[1].innerHTML === "true") ? "" : "-") + element.children[0].innerHTML;
          }, 1000);
        }
        
        %javascript%
      </script>
  </body>
</html>
