## Features
1. **Factions**
   - Players can create factions with custom names and colors.
   - Factions can invite new players to join
   - Factions can claim land
   - Factions can decalre war, make peace, and form alliances
   - Factions can create shops
   - Factions can gain or lose power in combat with players and mobs, or through commerce
   - Help/Info command
2. **Shops**
   - Implemented with Shopkeepers plugin API
   - Players can edit their faction's shop with a command
3. **Land Claims**
   - Factions can claim land based on how much power they have.
   - Claimed land can only be edited by Faction members
   - Factions at war can steal land from each other using power.
   - Land claims can be visualized using a command.
4. **Homes**
   - Factions may set homes to teleport between.
   - Number of homes scales with faction power.
   - Players may not use homes while in combat.

### TODO List:

1. **~~Tweak Power Change:~~ IMPLEMENTED**
    - [x] Make power change logarithmic for increased difficulty as power is gained (similar to enchanting levels in Minecraft).
2. **~~/rivals command~~ IMPLEMENTED**
    - [x] Add subcommand to check current invites. Should include invites to factions, alliances, and peace.
    - [x] Add a command to kick a member from a faction.
3. **Scoreboard**
    - [ ] Players should be shown their current faction name in its color, along with the faction's current power.
    - [ ] Display count of faction members and the number that are currently online.
4. **~~Faction Home:~~ IMPLEMENTED**
    - [x] Grant access to a faction home based on sufficient faction power.
    - [x] Scale the quantity of available homes with power.

5. **~~Time-Delay War Declarations:~~ IMPLEMENTED**
    - [x] Implement time-delayed war declarations.
    - [x] Immediate declarations have a power penalty, while time-delayed ones do not.
    - [x] Allow configuration of delay time in `config.yml`.

6. **~~Faction Ranking:~~** IMPLEMENTED
    - [x] Convert ranks from a list to a number stored with the faction upon serialization/deserialization.
    - [x] Consider reordering factions whenever `Faction.powerChange()` is called.

7. **~~Resource Chunks:~~** IMPLEMENTED
    - [x] Spawn resource chunks randomly with quantity controlled by `config.yml`.
    - [x] Allow resource chunks to have periodic resource spawn opportunities.
    - [x] Determine resource spawn based on a random chance, decreasing over time on a decay curve.
    - [x] Move resource chunk to a new location and reset its chance when the random chance falls below a configurable threshold.

8. **~~Politics:~~** IMPLEMENTED
    - [x] Enable factions to propose one resolution at a time.
    - [x] Allow voting for or against resolutions, with vote strength controlled by faction power.
    - [x] Pass resolutions with a majority vote after an allotted time.
    - [x] Adjust proposing faction's power change relative to the support for the proposal.
    - [x] Proposal Types:
        - [x] Denounce: Immediate loss of power relative to support.
        - [x] Sanction: Reduce power changes for perceived misbehavior.
        - [x] Unsanction: Remove sanctions.
        - [x] Intervention: Declare a faction a threat to all players.
        - [x] Change Setting: Modify a setting in the config.
        - [x] Custodian (Add/Remove): Custodian faction gets special powers.
            - [x] Set Budget: Declare the need for materials, rewarding providing factions with power.
            - [x] Set Mandate: Require Custodians to work towards a goal.
