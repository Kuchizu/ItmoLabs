from owlready2 import get_ontology, sync_reasoner_pellet
import re

onto = get_ontology("VideoGameOntology.owl").load()

with onto:
    sync_reasoner_pellet()

def parse_user_input(input_str):
    age = None
    preferences = []
    
    age_match = re.search(r"Мне (\d+) лет", input_str)
    if age_match:
        age = int(age_match.group(1))
    
    prefs_match = re.search(r"мне нравятся: (.+)", input_str)
    if prefs_match:
        prefs_str = prefs_match.group(1)
        preferences = [pref.strip() for pref in prefs_str.split(',')]
    
    return age, preferences

def parse_platform_input(input_str, valid_platforms):
    platforms = [plat.strip() for plat in input_str.split(',')]
    user_platforms = [plat for plat in platforms if plat in valid_platforms]
    return user_platforms

def parse_player_mode_input(input_str, valid_player_modes):
    modes = [mode.strip() for mode in input_str.split(',')]
    user_modes = [mode for mode in modes if mode in valid_player_modes]
    return user_modes

def main():
    valid_genres = ['RPG', 'FPS', 'Strategy', 'ActionAdventure', 'MOBA', 'BattleRoyale', 'Sandbox', 'Sports']

    print(f"\nДоступные жанры: {', '.join(valid_genres)}")
    user_input = input("Введите информацию о себе и своих предпочтениях (например, 'Мне 13 лет, мне нравятся: RPG, FPS'): ")
    
    age, preferences = parse_user_input(user_input)
    
    if age is None or not preferences:
        print("Пожалуйста, введите информацию в правильном формате.")
        return
    
    print("\nВозраст пользователя:", age)
    print("Предпочтения пользователя:", preferences)
    
    genre_mapping = {
        'RPG': onto.RPG_Ind,
        'FPS': onto.FPS_Ind,
        'Strategy': onto.Strategy_Ind,
        'ActionAdventure': onto.ActionAdventure_Ind,
        'MOBA': onto.MOBA_Ind,
        'BattleRoyale': onto.BattleRoyale_Ind,
        'Sandbox': onto.Sandbox_Ind,
        'Sports': onto.Sports_Ind
    }
    
    user_genres = [pref for pref in preferences if pref in valid_genres]
    
    if not user_genres:
        print("К сожалению, ваши предпочтения не совпадают с доступными жанрами.")
        return
    else:
        print("Жанры пользователя: ", user_genres, end='\n')
    
    valid_platforms = ['PC', 'PlayStation', 'Xbox', 'Nintendo_Switch']
    print(f"Доступные платформы: {', '.join(valid_platforms)}\n")
    platform_input = input(f"На каких платформах вы предпочитаете играть? (например, '{', '.join(valid_platforms)})': ")
    user_platforms = parse_platform_input(platform_input, valid_platforms)
    
    if not user_platforms:
        print("Вы не указали доступные платформы. Будут учитываться все платформы.")
    
    platform_mapping = {
        'PC': onto.PC,
        'PlayStation': onto.PlayStation,
        'Xbox': onto.Xbox,
        'Nintendo_Switch': onto.Nintendo_Switch
    }
    
    valid_player_modes = ['SinglePlayer', 'Multiplayer', 'Coop']
    print(f"Доступные режимы игры: {', '.join(valid_player_modes)}\n")
    player_mode_input = input("Какой режим игры вы предпочитаете? (например, 'SinglePlayer, Multiplayer'): ")
    user_player_modes = parse_player_mode_input(player_mode_input, valid_player_modes)
    
    if not user_player_modes:
        print("Вы не указали доступные режимы игры. Будут учитываться все режимы.")
    
    player_mode_mapping = {
        'SinglePlayer': onto.SinglePlayer_,
        'Multiplayer': onto.Multiplayer_,
        'Coop': onto.Coop_
    }
    
    def find_matching_games(consider_platforms=True, consider_player_modes=True):
        matching_games = set()
        for genre_name in user_genres:
            genre_individual = genre_mapping[genre_name]
            games_with_genre = onto.search(hasGenre=genre_individual)
            matching_games.update(games_with_genre)
        
        final_matching_games = set()
        for game in matching_games:
            if consider_platforms and user_platforms:
                game_platforms = [plat.name for plat in game.runsOn]
                if not any(plat in game_platforms for plat in user_platforms):
                    continue

            if consider_player_modes and user_player_modes:
                game_player_modes = [mode.name for mode in game.hasPlayerMode]
                if not any(mode in game_player_modes for mode in user_player_modes):
                    continue
            final_matching_games.add(game)

        return final_matching_games

    games = find_matching_games()
    if len(games) >= 2:
        recommended_games = games
    else:
        # print("\nИщем игры, соответствующие вашим жанрам и платформам, независимо от режима игры...")
        games = find_matching_games(consider_player_modes=False)
        if len(games) >= 2:
            recommended_games = games

        else:
            print("\nИщем игры, соответствующие вашим жанрам, независимо от платформ и режима игры...")
            games = find_matching_games(consider_platforms=False, consider_player_modes=False)
            if len(games) >= 2:
                recommended_games = games

            else:
                print("\nИщем любые игры, соответствующие вашим жанрам...")
                recommended_games = set()
                for genre_name in user_genres:
                    genre_individual = genre_mapping[genre_name]
                    games_with_genre = onto.search(hasGenre=genre_individual)
                    recommended_games.update(games_with_genre)

                if len(recommended_games) < 2:
                    print("\nК сожалению, не удалось найти достаточно игр по вашим предпочтениям. Рекомендуем другие популярные игры:")
                    recommended_games = set(onto.Game.instances())

    recommended_games = list(recommended_games)[:4]

    if recommended_games:
        print("\nМы рекомендуем вам следующие игры:")
        for game in recommended_games:
            print("- {}".format(game.name.replace('_', ' ')))

    else:
        print("К сожалению, мы не нашли игр, соответствующих вашим предпочтениям.")

if __name__ == "__main__":
    main()
