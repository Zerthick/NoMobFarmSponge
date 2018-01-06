# NoMobFarm - Prevent Players from building Auto-Mobfarms

## The Idea
The idea behind NoMobFarm is simple, in vanilla Minecraft, players can build contraptions that will kill mobs on their behalf and collect their drops. The aim of NoMobFarm is to prevent this behavior. It does this through the use of a simple config file located at ~/mods/config/NoMobFarm.conf. The default config is shown below:

```
# If set to true, mobs will not drop items unless damaged by the player within 5 seconds
DropRule=true
# If set to true, mobs from spawners will not drop items
PreventSpawnerDrops=true
# If set to true, mobs from spawners will not drop exp orbs
PreventSpawnerExp=true
```

The first node instates a 5 second drop rule in a way similar to how vanilla Minecraft restricts experience orbs, a mob will only drop items when killed if a player damaged it either directly or indirectly within the last 5 seconds

The second two rules disable drops and experience orbs from mobs originating from mob spawners across the board, regardless of how they perished 

## Support Me
I will **never** charge money for the use of my plugins, however they do require a significant amount of work to maintain and update. If you'd like to show your support and buy me a cup of tea sometime (I don't drink that horrid coffee stuff :P) you can do so [here](https://www.paypal.me/zerthick)
