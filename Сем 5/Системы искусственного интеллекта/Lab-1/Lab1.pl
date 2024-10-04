% Факты с одним аргументом (видеоигры и их жанры)

genre("The Witcher 3", rpg).
genre("Dark Souls", rpg).
genre("Overwatch", fps).
genre("Counter-Strike", fps).
genre("FIFA", sports).
genre("NBA 2K", sports).
genre("Civilization VI", strategy).
genre("Age of Empires II", strategy).
genre("Minecraft", sandbox).
genre("Terraria", sandbox).
genre("League of Legends", moba).
genre("Dota 2", moba).
genre("Fortnite", battle_royale).
genre("PUBG", battle_royale).
genre("Red Dead Redemption 2", action_adventure).
genre("GTA V", action_adventure).
genre("Super Mario Odyssey", platformer).
genre("Hades", roguelike).
genre("Among Us", party).
genre("Fall Guys", party).
genre("Assassin's Creed", action_adventure).
genre("Call of Duty", fps).
genre("Starcraft", strategy).
genre("Apex Legends", battle_royale).
genre("The Sims", simulation).
genre("Cities: Skylines", simulation).
genre("Rocket League", sports).

% Факты с двумя аргументами (связь между играми и разработчиками)

developer("The Witcher 3", "CD Projekt Red").
developer("Dark Souls", "FromSoftware").
developer("Overwatch", "Blizzard").
developer("Counter-Strike", "Valve").
developer("FIFA", "EA Sports").
developer("NBA 2K", "2K Sports").
developer("Civilization VI", "Firaxis Games").
developer("Age of Empires II", "Ensemble Studios").
developer("Minecraft", "Mojang").
developer("Terraria", "Re-Logic").
developer("League of Legends", "Riot Games").
developer("Dota 2", "Valve").
developer("Fortnite", "Epic Games").
developer("PUBG", "PUBG Corporation").
developer("Red Dead Redemption 2", "Rockstar Games").
developer("GTA V", "Rockstar Games").
developer("Super Mario Odyssey", "Nintendo").
developer("Assassin's Creed", "Ubisoft").
developer("Call of Duty", "Activision").
developer("Starcraft", "Blizzard").
developer("Apex Legends", "Respawn Entertainment").
developer("The Sims", "EA").
developer("Cities: Skylines", "Colossal Order").
developer("Rocket League", "Psyonix").

% Факты с тремя аргументами (платформы)

:- discontiguous platform/3.
:- discontiguous platform/2.

platform("The Witcher 3", "PC", "PS4").
platform("Dark Souls", "PC", "PS4").
platform("Overwatch", "PC", "PS4").
platform("Counter-Strike", "PC").
platform("FIFA", "PC", "PS4").
platform("NBA 2K", "PC", "PS4").
platform("Civilization VI", "PC").
platform("Age of Empires II", "PC").
platform("Minecraft", "PC", "Xbox").
platform("Terraria", "PC", "PS4").
platform("League of Legends", "PC").
platform("Dota 2", "PC").
platform("Fortnite", "PC", "PS4").
platform("PUBG", "PC", "PS4").
platform("Red Dead Redemption 2", "PC", "PS4").
platform("GTA V", "PC", "PS4").
platform("Super Mario Odyssey", "Switch").
platform("Assassin's Creed", "PC", "PS4").
platform("Call of Duty", "PC", "PS4").
platform("Starcraft", "PC").
platform("Apex Legends", "PC", "PS4").
platform("The Sims", "PC").
platform("Cities: Skylines", "PC").
platform("Rocket League", "PC", "PS4").

% Правила для определения типов игр

is_rpg_game(X) :- genre(X, rpg).
is_fps_game(X) :- genre(X, fps).
is_strategy_game(X) :- genre(X, strategy).
is_battle_royale_game(X) :- genre(X, battle_royale).
is_sports_game(X) :- genre(X, sports).
is_sandbox_game(X) :- genre(X, sandbox).
is_simulation_game(X) :- genre(X, simulation).
is_action_adventure_game(X) :- genre(X, action_adventure).

% Правила для нахождения игр, разработанных одной и той же компанией

same_developer(X, Y) :- developer(X, Z), developer(Y, Z).

% Правило для многожанровых игр

multigenre_game(X) :- genre(X, Genre1), genre(X, Genre2), Genre1 \= Genre2.

% Правила для определения популярных игр

popular_game(X) :- genre(X, rpg); genre(X, fps); genre(X, battle_royale).

% Правило для нахождения стратегических игр и разработчиков, которые их создают

strategic_developer(Y) :- developer(X, Y), genre(X, strategy).

% Правило для поиска игр на определённой платформе

available_on_pc(X) :- platform(X, "PC").
available_on_console(X) :- platform(X, "PS4").

% Правило для игр с многопользовательским режимом

multiplayer_game(X) :- genre(X, moba); genre(X, fps); genre(X, battle_royale).

% Новое правило для определения игр, которые являются симуляторами

is_simulator(X) :- genre(X, simulation).

% Новое правило для нахождения игр, разработанных "Blizzard" и их жанров

blizzard_game(X) :- developer(X, "Blizzard").

% Правило для определения игр на Nintendo Switch

available_on_switch(X) :- platform(X, "Switch", _).
