MineralLimiter
========

A Craftbukkit plugin that prevents players from mining too much minerals in spefific worlds.

Usage
========

* Shoo your players away to mining worlds.
* Limiting how much they can mine per period.


Commands
========
* **/mlimit reload** - Reloads the plugin.

Permissions
========

    MineralLimiter.*:
        description: Gives access to all MineralLimiter commands.
        default: op
    MineralLimiter.admin:
        description: Gives access to all MineralLimiter administrative commands.
        children:
            MineralLimiter.ignore: true
            MineralLimiter.reload: true
        default: op
    MineralLimiter.ignore:
        description: Players with this permission will not be affected by this plugin.
        default: op
    MineralLimiter.reload:
        description: Reloads the plugin.
        default: op
