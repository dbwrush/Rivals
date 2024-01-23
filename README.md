🌟Documentation🌟

### TODO List:

- [x] Completed task
- [~] Inapplicable task
- [ ] Incomplete task
  - [x] Sub-task 1
  - [~] Sub-task 2
  - [ ] Sub-task 3

1. [x] Completed task
1. [~] Inapplicable task
1. [ ] Incomplete task
   1. [x] Sub-task 1
   1. [~] Sub-task 2
   1. [ ] Sub-task 3

* [x]
1. **~~Tweak Power Change:~~ IMPLEMENTED**
    - Make power change logarithmic for increased difficulty as power is gained (similar to enchanting levels in Minecraft).

2. **Faction Home:**
    - Grant access to a faction home based on sufficient faction power.
    - Scale the quantity of available homes with power.

3. **Time-Delay War Declarations:**
    - Implement time-delayed war declarations.
    - Immediate declarations have a power penalty, while time-delayed ones do not.
    - Allow configuration of delay time in `config.yml`.

4. **Faction Ranking:**
    - Convert ranks from a list to a number stored with the faction upon serialization/deserialization.
    - Reorder factions based on power, not creation date, when calling the faction list.
    - Consider reordering factions whenever `Faction.powerChange()` is called.

5. **Resource Chunks:**
    - Spawn resource chunks randomly with quantity controlled by `config.yml`.
    - Allow resource chunks to have periodic resource spawn opportunities.
    - Determine resource spawn based on a random chance, decreasing over time on a decay curve.
    - Move resource chunk to a new location and reset its chance when the random chance falls below a configurable threshold.

6. **Politics:**
    - Enable factions to propose one resolution at a time.
    - Allow voting for or against resolutions, with vote strength controlled by faction power.
    - Pass resolutions with a majority vote after an allotted time.
    - Adjust proposing faction's power change relative to the support for the proposal.
    - Proposal Types:
        - Denounce: Immediate loss of power relative to support.
        - Sanction: Reduce power changes for perceived misbehavior.
        - Unsanction: Remove sanctions.
        - Intervention: Declare a faction a threat to all players.
        - Change Setting: Modify a setting in the config.
        - Custodian (Add/Remove): Custodian faction gets special powers.
            - Set Budget: Declare the need for materials, rewarding providing factions with power.
            - Set Mandate: Require Custodians to work towards a goal.
