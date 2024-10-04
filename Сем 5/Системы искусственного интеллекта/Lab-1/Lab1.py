from owlready2 import *

# Создание нового онтологического файла
onto = get_ontology("http://yourontology.org/VideoGameOntology.owl")

with onto:
    # Создание классов
    class Game(Thing):
        pass

    class Genre(Thing):
        pass

    class Developer(Thing):
        pass

    class Platform(Thing):
        pass  # Добавляем платформы, на которых могут работать игры
    
    class PlayerMode(Thing):
        pass  # Добавляем режимы игры (одиночная, многопользовательская и т.д.)

    # Создание подклассов для жанров
    class RPG(Genre):
        pass

    class FPS(Genre):
        pass

    class Strategy(Genre):
        pass

    class ActionAdventure(Genre):
        pass

    class MOBA(Genre):
        pass

    class BattleRoyale(Genre):
        pass

    class Sandbox(Genre):
        pass

    class Sports(Genre):
        pass

    # Создание подклассов для режимов игры
    class SinglePlayer(PlayerMode):
        pass

    class Multiplayer(PlayerMode):
        pass

    class Coop(PlayerMode):
        pass

    # Свойства
    class hasGenre(ObjectProperty):
        domain = [Game]
        range = [Genre]

    class developedBy(ObjectProperty):
        domain = [Game]
        range = [Developer]

    class runsOn(ObjectProperty):
        domain = [Game]
        range = [Platform]  # Связь с платформой

    class hasPlayerMode(ObjectProperty):
        domain = [Game]
        range = [PlayerMode]  # Связь с режимом игры

    # Добавление индивидов для жанров
    rpg_genre_ind = RPG("RPG_Ind")
    fps_genre_ind = FPS("FPS_Ind")
    strategy_genre_ind = Strategy("Strategy_Ind")
    action_adventure_genre_ind = ActionAdventure("ActionAdventure_Ind")
    moba_genre_ind = MOBA("MOBA_Ind")
    battle_royale_genre_ind = BattleRoyale("BattleRoyale_Ind")
    sandbox_genre_ind = Sandbox("Sandbox_Ind")
    sports_genre_ind = Sports("Sports_Ind")

    # Добавление индивидов для разработчиков
    cd_projekt_red = Developer("CD_Projekt_Red")
    from_software = Developer("FromSoftware")
    blizzard = Developer("Blizzard")
    valve = Developer("Valve")
    ea_sports = Developer("EA_Sports")
    riot_games = Developer("Riot_Games")
    epic_games = Developer("Epic_Games")
    mojang = Developer("Mojang")
    pubg_corp = Developer("PUBG_Corp")
    ensemble_studios = Developer("Ensemble_Studios")

    # Добавление индивидов для платформ
    pc = Platform("PC")
    playstation = Platform("PlayStation")
    xbox = Platform("Xbox")
    nintendo_switch = Platform("Nintendo_Switch")

    # Добавление индивидов для режимов игры
    singleplayer_mode = SinglePlayer("SinglePlayer_")
    multiplayer_mode = Multiplayer("Multiplayer_")
    coop_mode = Coop("Coop_")

    # Добавление индивидов для игр
    the_witcher_3 = Game("The_Witcher_3")
    dark_souls = Game("Dark_Souls")
    overwatch = Game("Overwatch")
    dota_2 = Game("Dota_2")
    league_of_legends = Game("League_of_Legends")
    fortnite = Game("Fortnite")
    fifa = Game("FIFA")
    minecraft = Game("Minecraft")
    pubg = Game("PUBG")
    age_of_empires_ii = Game("Age_of_Empires_II")

    # Установление связей для каждой игры
    the_witcher_3.hasGenre.append(rpg_genre_ind)
    the_witcher_3.developedBy.append(cd_projekt_red)
    the_witcher_3.runsOn.append(pc)
    the_witcher_3.runsOn.append(playstation)
    the_witcher_3.hasPlayerMode.append(singleplayer_mode)

    dark_souls.hasGenre.append(rpg_genre_ind)
    dark_souls.developedBy.append(from_software)
    dark_souls.runsOn.append(pc)
    dark_souls.runsOn.append(playstation)
    dark_souls.hasPlayerMode.append(singleplayer_mode)

    overwatch.hasGenre.append(fps_genre_ind)
    overwatch.developedBy.append(blizzard)
    overwatch.runsOn.append(pc)
    overwatch.runsOn.append(playstation)
    overwatch.hasPlayerMode.append(multiplayer_mode)

    dota_2.hasGenre.append(moba_genre_ind)
    dota_2.developedBy.append(valve)
    dota_2.runsOn.append(pc)
    dota_2.hasPlayerMode.append(multiplayer_mode)

    league_of_legends.hasGenre.append(moba_genre_ind)
    league_of_legends.developedBy.append(riot_games)
    league_of_legends.runsOn.append(pc)
    league_of_legends.hasPlayerMode.append(multiplayer_mode)

    fortnite.hasGenre.append(battle_royale_genre_ind)
    fortnite.developedBy.append(epic_games)
    fortnite.runsOn.append(pc)
    fortnite.runsOn.append(playstation)
    fortnite.runsOn.append(xbox)
    fortnite.hasPlayerMode.append(multiplayer_mode)

    fifa.hasGenre.append(sports_genre_ind)
    fifa.developedBy.append(ea_sports)
    fifa.runsOn.append(pc)
    fifa.runsOn.append(playstation)
    fifa.runsOn.append(xbox)
    fifa.hasPlayerMode.append(multiplayer_mode)

    minecraft.hasGenre.append(sandbox_genre_ind)
    minecraft.developedBy.append(mojang)
    minecraft.runsOn.append(pc)
    minecraft.runsOn.append(playstation)
    minecraft.runsOn.append(xbox)
    minecraft.runsOn.append(nintendo_switch)
    minecraft.hasPlayerMode.append(singleplayer_mode)
    minecraft.hasPlayerMode.append(multiplayer_mode)

    pubg.hasGenre.append(battle_royale_genre_ind)
    pubg.developedBy.append(pubg_corp)
    pubg.runsOn.append(pc)
    pubg.runsOn.append(playstation)
    pubg.runsOn.append(xbox)
    pubg.hasPlayerMode.append(multiplayer_mode)

    age_of_empires_ii.hasGenre.append(strategy_genre_ind)
    age_of_empires_ii.developedBy.append(ensemble_studios)
    age_of_empires_ii.runsOn.append(pc)
    age_of_empires_ii.hasPlayerMode.append(singleplayer_mode)
    age_of_empires_ii.hasPlayerMode.append(multiplayer_mode)

# Сохранение в файл
onto.save("VideoGameOntology.owl")
