# Shattered Pixel Dungeon Web Multiplayer

This context names the lightweight web multiplayer prototype for Shattered Pixel Dungeon. It describes how peer roles and floor-race rules are discussed without prescribing implementation details.

## Language

**Room Host**:
The player whose URL `id` is `1` and whose current run is the source copied by later peers.
_Avoid_: server, authority, master

**Room Owner**:
The room participant who controls lobby settings and starts the multiplayer game.
_Avoid_: host, leader, authority

**Room Owner Transfer**:
The reassignment of room-control responsibility before the multiplayer game starts.
_Avoid_: host migration, authority election, leader election

**Room Leave**:
The deliberate departure of a Room Participant from the multiplayer room.
_Avoid_: disconnect, timeout, crash

**Room Disconnect**:
The loss of a Room Participant from the multiplayer room without an explicit Leave action.
_Avoid_: crash, network failure, socket close

**Room Reconnect**:
The return of a Room Participant before Leave cleanup completes.
_Avoid_: rejoin, new join, replacement

**Reconnect Deadline**:
The room-agreed cutoff before a Room Disconnect completes Leave cleanup.
_Avoid_: local timeout, grace guess, peer timer

**Reconnect Token**:
The participant secret that proves a returning connection can reclaim the same Room Participant.
_Avoid_: player name, room password, room id

**Player Watch Return**:
The return of a Playable Player eliminated by disconnect timeout as a Watcher in the same active room.
_Avoid_: late join, live reconnect, new watcher

**Room Password**:
The shared secret participants use to enter the same multiplayer room.
_Avoid_: invite code, transport key, server password

**Room Message Encryption**:
The protection that lets participants with the Room Password read and validate room protocol messages.
_Avoid_: room authority, gameplay fairness, transport privacy

**Degraded Transport Conditions**:
Peer-to-peer message delivery conditions where room or active messages may be delayed, duplicated, reordered, dropped, or temporarily unavailable while participants are still trying to use the same multiplayer room.
_Avoid_: poor network, bad wifi, lag, packet loss

**Room Name**:
The visible label chosen for a multiplayer room.
_Avoid_: room id, lobby id, transport room

**Room Name Conflict**:
A Create Room failure caused by an active room with the same Room Name and Room Password.
_Avoid_: duplicate room, join suggestion, name taken

**Room Create Probe**:
The create-time check for an active room before the first Room Snapshot is issued.
_Avoid_: join, server reservation, room lock

**Room Create Race**:
A simultaneous Create Room attempt where more than one Room Create Probe finds no active room.
_Avoid_: duplicate owner, split room, second room

**Room Join Request**:
The normal room UI request to enter an existing multiplayer room before lobby state is shown.
_Avoid_: Joiner, reconnect, Player Watch Return

**Dev URL Path**:
The direct URL-parameter multiplayer entry flow kept for development compatibility.
_Avoid_: normal room UI, public join flow, lobby flow

**Participant Source**:
A debug-only label for how a room participant entered, such as normal room UI or Dev URL Path.
_Avoid_: role, authority, trust level, player-facing badge

**Room Phase**:
The lifecycle stage that decides which room actions can currently happen.
_Avoid_: screen, UI mode, transport state

**Room Epoch**:
The room-state generation that distinguishes current room decisions from stale ones.
_Avoid_: timestamp, frame, peer order

**Participant Intent**:
A Room Participant's requested change before it becomes accepted room state.
_Avoid_: command, patch, final state

**Intent Id**:
The identifier that makes a Participant Intent safe to receive more than once.
_Avoid_: action id, message id, nonce

**Intent Result**:
The response that tells a Room Participant whether a Participant Intent was accepted or rejected.
_Avoid_: Room Snapshot, broadcast state, peer event

**Pending Intent**:
A Participant Intent waiting for a Room Snapshot to accept or reject it.
_Avoid_: optimistic room state, local truth, accepted state

**Room Snapshot**:
The accepted room state for a Room Phase and Room Epoch.
_Avoid_: local truth, partial patch, peer opinion

**Room Participant**:
A room member that is either a Playable Player or a Watcher.
_Avoid_: peer, user, connection

**Player Name**:
The display name chosen by a Room Participant.
_Avoid_: participant id, account name, login

**Participant Color**:
The display color assigned to a Room Participant's Player Name.
_Avoid_: team, skin, participant id

**Player Seat**:
A lobby place for a Room Participant who will start as a Playable Player.
_Avoid_: save slot, room slot, chair

**Saved Game**:
A durable single-player run stored in a normal progress slot.
_Avoid_: Player Seat, Watcher Slot, Transient Run State

**Transient Run State**:
Same-session multiplayer dungeon state that supports a Competitive Run or Watcher View without becoming a Saved Game.
_Avoid_: save slot, saved game, checkpoint

**Ready State**:
A Player Seat signal that the participant is prepared for the multiplayer game to start.
_Avoid_: vote, confirmation, lock-in

**Hero Choice**:
The hero class selected for a Player Seat.
_Avoid_: loadout, character pick, avatar

**Competitive Run**:
A fresh multiplayer run started from a Player Seat for speed-run competition.
_Avoid_: clone, copy, shared run, saved game

**Multiplayer Journal Availability**:
The rule that a multiplayer game treats journal pages as available so discovery gates do not shape competitive runs.
_Avoid_: saved journal progress, collected pages, tutorial completion

**Room Winner**:
The Playable Player whose result wins the multiplayer room.
_Avoid_: Room Owner, last connected participant, Watch Target

**Room Winner Claim**:
A peer-visible claim that a Competitive Run reached a multiplayer victory condition.
_Avoid_: Room Winner, local ranking, game-over state

**Shared Run Seed**:
The room-level seed that gives Competitive Runs the same generated dungeon conditions.
_Avoid_: room password, player seed, random room id

**Joiner**:
A player whose URL `id` is not `1` and who begins by copying the Room Host's current run.
_Avoid_: client, guest, slave

**Playable Player**:
A Room Host or Joiner that controls a local run.
_Avoid_: actor, controller, owner

**Peer Mirror**:
A non-interactive visual presence that represents another Playable Player in the local run.
_Avoid_: opponent, enemy, summon

**Peer Buff**:
An icon-bearing condition currently affecting a Playable Player and mirrored as part of that player's Peer Mirror presence.
_Avoid_: peer condition, peer debuff, remote buff object

**Peer Appearance**:
The visible hero class and armor tier of a Playable Player as shown by that player's Peer Mirror.
_Avoid_: skin sync, cosmetic state, avatar clone

**In-Game Player List**:
The multiplayer gameplay view of Playable Players and their current run status.
_Avoid_: party list, team list, scoreboard

**Watcher**:
A room participant that observes a selected player's run and cannot control a run.
_Avoid_: spectator, viewer, ghost

**Watcher View**:
The read-only game view shown to a Watcher, including logs and information panels but not controls that can change a run.
_Avoid_: playback mode, observer UI, disabled game

**Watcher Slot**:
A local, non-playable place where a Watcher holds the current Watch Target view.
_Avoid_: save slot, checkpoint, clone slot

**Watch Target**:
The Playable Player selected by a Watcher.
_Avoid_: host, source, stream

**Replay Stream**:
A Watch Target's ordered keyframes and replay events that let Watchers reconstruct a read-only view of that run.
_Avoid_: video stream, input sync, shared simulation

**Keyframe**:
A complete Watch Target view used to start or resync a Replay Stream.
_Avoid_: save, checkpoint, full sync

**Replay Event**:
A read-only observer event in a Replay Stream that describes a committed change in the Watch Target's run.
_Avoid_: play action, command, transaction

**Snapshot Clone**:
A local copy of the Room Host's current run used as the Joiner's starting point.
_Avoid_: sync, merge, shared save

**Floor Notice**:
A peer-visible message that a player has reached a dungeon floor.
_Avoid_: broadcast, alert event

**Floor Chase**:
The rule that a player who is behind another player's floor progress has 20 local hero turns per missing floor to catch up or their run ends.
_Avoid_: timeout, timer, race clock

## Relationships

- A **Room Host** can provide one **Snapshot Clone** to each **Joiner**.
- A **Room Owner** is distinct from a **Room Host**.
- A **Room Owner** can be a **Playable Player** or a **Watcher**.
- A **Room Owner** controls lobby settings before the multiplayer game starts.
- A **Room Owner** is the only participant who changes lobby rules, starts the countdown, or cancels an active countdown.
- A **Player Seat** does not reserve or require a **Saved Game**.
- A **Competitive Run** uses **Transient Run State** instead of becoming a **Saved Game**.
- A **Competitive Run** uses **Multiplayer Journal Availability** without changing saved journal progress.
- A **Room Leave** is explicit.
- A **Room Leave** before the multiplayer game starts cleans up immediately.
- A **Room Leave** from a **Player Seat** clears that **Player Seat**'s **Ready State**.
- A **Room Leave** during countdown cancels the countdown.
- A **Room Leave** releases room connection capacity.
- A **Room Disconnect** is treated like Leave after the room considers the participant gone.
- A **Room Disconnect** can start a **Reconnect Deadline**.
- A **Reconnect Deadline** can apply before or during a **Competitive Run**.
- A **Room Disconnect** during countdown cancels the countdown before Leave cleanup completes.
- A **Room Owner** must be connected for countdown to continue.
- A **Room Disconnect** from a **Player Seat** clears that **Player Seat**'s **Ready State** before Leave cleanup completes.
- A **Room Reconnect** before Leave cleanup preserves the same **Room Participant**.
- A **Room Reconnect** before the **Reconnect Deadline** prevents Leave cleanup.
- A **Room Reconnect** requires the same **Room Participant** and **Reconnect Token**.
- A **Room Reconnect** can preserve **Room Owner** status.
- A return after Leave cleanup completes is not a **Room Reconnect**.
- A return after Leave cleanup completes creates a new **Room Participant**.
- A return after Leave cleanup completes does not preserve **Room Owner** status, **Player Seat**, **Hero Choice**, or **Ready State**.
- A lost **Reconnect Token** prevents **Room Reconnect**.
- A **Room Disconnect** can reserve room connection capacity until Leave cleanup completes.
- A **Room Reconnect** uses the reserved room connection capacity.
- Leave cleanup after **Room Disconnect** happens at the **Reconnect Deadline**.
- Leave cleanup releases reserved room connection capacity.
- A **Playable Player** eliminated by disconnect timeout cannot return as a live **Playable Player** in that room.
- A disconnect-only outcome does not create a **Room Winner**.
- A **Room Winner Claim** can establish a **Room Winner**.
- A **Room Winner Claim** must come from a connected live **Playable Player**.
- Competing **Room Winner Claims** resolve deterministically.
- A **Playable Player** eliminated by disconnect timeout can use **Player Watch Return**.
- A **Player Watch Return** keeps the same **Room Participant** identity.
- A **Player Watch Return** keeps the same **Player Name** and **Participant Color**.
- A **Player Watch Return** does not restore **Player Seat**, **Hero Choice**, **Ready State**, or live **Playable Player** status.
- A **Player Watch Return** counts toward the room's connection limit while connected.
- A **Player Watch Return** is rejected when the room has no open connection capacity.
- A **Room Owner Transfer** happens when the current **Room Owner** leaves or disconnects before the multiplayer game starts.
- A **Room Owner Transfer** caused by **Room Disconnect** happens only after Leave cleanup.
- A **Room Reconnect** before Leave cleanup prevents **Room Owner Transfer**.
- A **Room Owner Transfer** chooses one deterministic-random remaining connected room participant, including **Watchers**.
- A **Room Owner Transfer** has one room-wide result.
- A disconnected **Room Participant** is not eligible for **Room Owner Transfer**.
- A **Room Owner Transfer** requires at least one connected remaining **Room Participant**.
- A **Room Owner Transfer** does not clear **Ready State** by itself.
- A **Room Owner** leaving a **Player Seat** still clears that **Player Seat**'s **Ready State**.
- A **Room Owner Transfer** is not needed during active gameplay.
- A **Room Name** belongs to a multiplayer room.
- A **Room Password** belongs to a multiplayer room.
- A **Room Name Conflict** blocks Create Room.
- A **Room Create Probe** can find a **Room Name Conflict**.
- A Create Room flow starts room ownership only after **Room Create Probe** finds no active room.
- A **Room Create Race** resolves to one initial **Room Owner**.
- A **Room Create Race** loser sees **Room Name Conflict**.
- A **Room Join Request** must be accepted before lobby state is shown.
- A **Room Join Request** can be rejected before creating a visible lobby participant.
- A **Dev URL Path** can bypass normal **Room Create Probe** and **Room Join Request** handshakes.
- A **Dev URL Path** does not define normal room UI behavior.
- A **Dev URL Path** sharing normal room transport still counts toward room connection capacity.
- A **Dev URL Path** can set **Participant Source** for debug/test surfaces.
- A **Participant Source** must not change room authority or normal player-facing lobby UI.
- A **Room Password** enables **Room Message Encryption**.
- **Room Message Encryption** protects room protocol messages in the public peer-to-peer transport.
- **Room Message Encryption** does not define **Room Owner** authority.
- **Degraded Transport Conditions** can delay or hide room messages without changing **Room Owner** authority.
- **Degraded Transport Conditions** can create **Pending Intents**, **Room Disconnects**, or **Replay Stream** gaps.
- **Degraded Transport Conditions** must not let stale room messages change accepted **Room Snapshot** state.
- A **Room Phase** gates which room actions can change room state.
- A **Room Epoch** advances when current room decisions make older room decisions stale.
- A room decision outside the current **Room Phase** or **Room Epoch** cannot change room state.
- A **Room Participant** can express a **Participant Intent**.
- A **Participant Intent** has an **Intent Id**.
- An **Intent Id** prevents the same **Participant Intent** from changing room state more than once.
- A **Room Owner** validates **Participant Intents** before they become accepted room state.
- A **Room Owner** can answer an accepted **Participant Intent** with a **Room Snapshot**.
- A **Room Owner** answers a rejected **Participant Intent** with an **Intent Result**.
- An **Intent Result** belongs to one **Intent Id**.
- An **Intent Result** can explain why a **Participant Intent** was rejected.
- A **Pending Intent** is not accepted room state.
- A **Pending Intent** can make local controls feel responsive before a **Room Snapshot** arrives.
- A **Pending Intent** can clear without changing accepted room state.
- A newer **Room Snapshot** can clear a **Pending Intent**.
- A **Room Owner Transfer** can clear **Pending Intents**.
- A **Room Snapshot** represents accepted room state for one **Room Phase** and **Room Epoch**.
- A **Room Snapshot** is the room state other participants should render.
- A **Room Snapshot** does not contain **Room Password** or **Reconnect Token** secrets.
- A **Room Owner Transfer** advances the **Room Epoch**.
- A **Room Owner Transfer** makes older **Room Snapshots** stale.
- A **Room Owner Transfer** can require **Room Participants** to express still-relevant **Participant Intents** again.
- A **Room Participant** has one **Player Name**.
- A **Room Participant** has one **Participant Color**.
- A **Room Participant** has one **Reconnect Token**.
- Multiple **Room Participants** can share the same **Player Name**.
- A **Player Name** does not prove **Room Participant** identity.
- A multiplayer room has up to four **Player Seats**.
- A **Room Participant** enters the first open **Player Seat** by default.
- A **Room Participant** enters as a **Watcher** by default when no **Player Seat** is open.
- Before the multiplayer game starts, a room can remain open with only **Watchers** if at least one **Room Participant** is connected.
- A multiplayer game cannot start without an occupied **Player Seat**.
- Before the multiplayer game starts, a **Watcher** can move into an open **Player Seat**.
- A **Watcher** moving into a **Player Seat** uses the first open **Player Seat**.
- A **Watcher** moving into a **Player Seat** starts without **Ready State**.
- A **Watcher** moving into a **Player Seat** gets that **Player Seat**'s default **Hero Choice**.
- A **Watcher** moving into a **Player Seat** can keep **Room Owner** status.
- A **Watcher** cannot move into a **Player Seat** when every **Player Seat** is occupied.
- A seatless **Watcher** has no **Hero Choice**.
- A **Ready State** belongs to a **Player Seat**.
- A **Watcher** has no **Ready State**.
- A **Room Reconnect** does not restore **Ready State**.
- Leaving a **Player Seat** clears its **Ready State**.
- A disconnected **Room Participant** can keep a **Player Seat** until Leave cleanup completes.
- A **Hero Choice** belongs to a **Player Seat**.
- Changing a **Hero Choice** clears that **Player Seat**'s **Ready State**.
- Freeing a **Player Seat** clears that **Player Seat**'s **Hero Choice**.
- A normal room launch starts one **Competitive Run** for each **Player Seat**.
- A **Competitive Run** uses its **Player Seat**'s **Hero Choice**.
- **Competitive Runs** in the same room share one **Shared Run Seed**.
- A **Shared Run Seed** makes dungeon generation match across **Competitive Runs**.
- A **Shared Run Seed** does not choose **Hero Choice**.
- A **Shared Run Seed** is created with the room.
- A **Shared Run Seed** stays fixed for the room's lifetime.
- Changing lobby rules does not change the **Shared Run Seed**.
- Canceling countdown does not change the **Shared Run Seed**.
- Different **Player Seats** can have different **Hero Choices**.
- Changing lobby rules clears every **Player Seat**'s **Ready State**.
- Changing lobby rules cancels an active countdown.
- After the multiplayer game starts, the room does not accept new **Room Participants**.
- A **Joiner** continues as an independent player after applying a **Snapshot Clone**.
- A **Peer Mirror** represents a **Playable Player** other than the local player.
- A **Peer Mirror** is derived from the represented **Playable Player**'s **Replay Stream**.
- A **Peer Mirror** does not have a separate observer stream from **Replay Stream**.
- A **Peer Mirror** can use a **Keyframe** to recover from a **Replay Stream** gap.
- A **Peer Mirror** is shown only when the represented **Playable Player** is on the same dungeon depth and branch as the local player.
- A **Peer Mirror** is shown only where the local player has current field of view.
- A **Peer Mirror** can remember a hidden player's latest known state without showing it.
- A **Peer Mirror** uses movement-only animation in the first version.
- A **Peer Mirror** is absent from cell inspection and hit-testing.
- A **Peer Buff** belongs to the same Playable Player represented by a **Peer Mirror**.
- A **Peer Buff** may be shown as compact non-interactive icon decoration on a **Peer Mirror**, but it does not create local `Buff`, `Char`, `Mob`, `Actor`, collision, or inspection state.
- A **Peer Appearance** belongs to the same Playable Player represented by a **Peer Mirror**.
- A **Watcher** observes one **Watch Target** at a time.
- A **Watcher** uses a **Watcher View**.
- A **Watcher View** uses a **Watcher Slot** rather than a playable run slot.
- A **Watch Target** must be a **Playable Player**.
- A **Replay Stream** belongs to one **Watch Target**.
- A **Replay Stream** starts from a **Keyframe**.
- A **Replay Stream** contains **Replay Events**.
- A **Replay Stream** has one ordering sequence owned by its source Playable Player.
- A **Replay Stream** can use a slow heartbeat from its source Playable Player.
- A **Replay Stream** heartbeat republishes ordinary self-describing **Replay Events** rather than a special heartbeat event.
- A **Replay Event** is weaker than a Play Action and is not used to control a run.
- A **Replay Event** uses a small observer catalog: move, attack, interact, item, wait, search, transition, log, status, or unknown.
- A transition **Replay Event** is the observer-stream signal for source depth or branch changes.
- An unknown **Replay Event** marks a watched change that should be resynced rather than animated.
- A **Watcher View** plays a **Replay Stream** from ordered **Replay Events**, with buffering allowed for smoothness.
- A **Peer Mirror** projects a **Replay Stream** without becoming a **Watcher View**.
- A **Peer Mirror** first projects only move, transition, and status **Replay Events**.
- A **Peer Mirror** does not project log **Replay Events**.
- A move **Replay Event** carries the source player's current placement, not only a movement delta.
- A status **Replay Event** carries the current complete **Peer Buff** list, not only buff changes.
- Only the **Watch Target** publishes its own **Replay Stream**.
- When a **Watcher** changes **Watch Target**, the Watcher leaves the old **Replay Stream** and starts from the new target's latest view.
- An **In-Game Player List** shows **Playable Players**.
- An **In-Game Player List** can show each **Playable Player**'s **Player Name**, **Participant Color**, **Peer Appearance**, current health, and **Peer Buffs**.
- An **In-Game Player List** can keep non-live **Playable Players** visible.
- Non-live **Playable Players** in the **In-Game Player List** are not selectable as new **Watch Targets**.
- In **Watcher View**, selecting a **Playable Player** in the **In-Game Player List** changes the **Watch Target**.
- A **Watcher** entering gameplay without an explicit **Watch Target** defaults to the first live **Player Seat** by seat order.
- A defeated **Playable Player** who continues as a **Watcher** defaults to the first remaining live **Player Seat** by seat order.
- A **Watcher** can keep a non-live **Watch Target**'s last known view.
- A **Watcher** does not change **Watch Target** only because the current **Watch Target** becomes non-live.
- If a **Room Winner** is known, a **Watcher** sees the multiplayer winning UI instead of changing **Watch Target**.
- A **Floor Notice** can start or advance a **Floor Chase** for any player on an earlier floor, but is not the observer-stream floor-state signal.
- A **Floor Chase** is measured in local hero turns, not wall-clock time.

## Example dialogue

> **Dev:** "When a **Joiner** appears, do they keep syncing every move from the **Room Host**?"
> **Domain expert:** "No, they take one **Snapshot Clone** and then only share **Floor Notices** for the **Floor Chase**."

## Flagged ambiguities

- "multiplayer" does not mean shared combat or shared actions in this prototype; it means independent local runs connected by **Floor Notices** and the **Floor Chase**.
- In lobby wording, "host" means **Room Owner**; **Room Host** remains the URL `id=1` source-run role.
- In watcher URLs, `id` names the **Watcher** and `watch` names the **Watch Target**.
- A **Watcher View** can show inventory information, but inventory actions do not control the watched run.
- In a **Watcher View**, the **Watch Target** owns camera position, zoom, and view focus.
- A **Watcher View** can allow passive inspection of visible information without changing the watched camera.
- A **Watcher** does not participate in **Floor Chase**.
- A **Watcher View** can show the **Watch Target**'s **Floor Chase** state.
