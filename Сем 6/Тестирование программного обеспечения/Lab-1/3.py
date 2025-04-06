import unittest
import uuid

class HyperIntelligentBeing:
    def __init__(self, name, age):
        self.name = name
        self.age = age
        self.energy = 100
        self.id = uuid.uuid4()

    def solve_problem(self):
        return f"{self.name} решает главный вопрос жизни."

    def play_ultra_cricket(self):
        if self.energy <= 0:
            raise ValueError(f"{self.name} не может играть, энергии не хватает.")

        self.energy -= 10
        return f"{self.name} играет в ультра-крикет."

    def recharge_energy(self):
        self.energy = 100
        return f"{self.name} восстановил энергию."

class UltraCricketGame:
    def __init__(self):
        self.players = []
        self.score = 0

    def add_player(self, player):
        if player.id in [p.id for p in self.players]:
            raise ValueError(f"Игрок {player.name} уже в игре.")
        self.players.append(player)

    def start_game(self):
        if not self.players:
            raise ValueError("Невозможно начать игру без игроков.")

        game_status = []
        for player in self.players:
            game_status.append(player.play_ultra_cricket())
        return game_status

    def score_game(self):
        self.score = len(self.players) * 10
        return f"Итоговый счет: {self.score}"

    def remove_player(self, player):
        if player.id in [p.id for p in self.players]:
            self.players.remove(player)
            return f"Игрок {player.name} удален из игры."
        return f"Игрок {player.name} не найден в игре."

class TestDomainModel(unittest.TestCase):
    def test_hyper_intelligent_beings(self):
        being = HyperIntelligentBeing("Зорас", 5000)
        self.assertEqual(being.solve_problem(), "Зорас решает главный вопрос жизни.")
        self.assertEqual(being.play_ultra_cricket(), "Зорас играет в ультра-крикет.")
        self.assertEqual(being.recharge_energy(), "Зорас восстановил энергию.")

    def test_multiple_hyper_intelligent_beings(self):
        beings = [
            HyperIntelligentBeing("Зорас", 5000),
            HyperIntelligentBeing("Глитос", 4000),
            HyperIntelligentBeing("Тирон", 6000)
        ]
        self.assertEqual(beings[0].solve_problem(), "Зорас решает главный вопрос жизни.")
        self.assertEqual(beings[1].solve_problem(), "Глитос решает главный вопрос жизни.")
        self.assertEqual(beings[2].solve_problem(), "Тирон решает главный вопрос жизни.")

    def test_ultra_cricket_game(self):
        game = UltraCricketGame()
        player1 = HyperIntelligentBeing("Зорас", 5000)
        player2 = HyperIntelligentBeing("Глитос", 4000)
        
        game.add_player(player1)
        game.add_player(player2)
        
        game_status = game.start_game()
        self.assertIn("Зорас играет в ультра-крикет.", game_status)
        self.assertIn("Глитос играет в ультра-крикет.", game_status)
        
        score = game.score_game()
        self.assertEqual(score, "Итоговый счет: 20")

    def test_game_with_no_players(self):
        game = UltraCricketGame()
        with self.assertRaises(ValueError):
            game.start_game()

    def test_game_with_one_player(self):
        game = UltraCricketGame()
        player1 = HyperIntelligentBeing("Зорас", 5000)
        game.add_player(player1)
        
        game_status = game.start_game()
        self.assertIn("Зорас играет в ультра-крикет.", game_status)
        
        score = game.score_game()
        self.assertEqual(score, "Итоговый счет: 10")

    def test_remove_player(self):
        game = UltraCricketGame()
        player1 = HyperIntelligentBeing("Зорас", 5000)
        player2 = HyperIntelligentBeing("Глитос", 4000)
        
        game.add_player(player1)
        game.add_player(player2)
        
        self.assertEqual(len(game.players), 2)
        game.remove_player(player1)
        self.assertEqual(len(game.players), 1)

    def test_player_duplicates(self):
        game = UltraCricketGame()
        player1 = HyperIntelligentBeing("Зорас", 5000)
        game.add_player(player1)
        with self.assertRaises(ValueError):
            game.add_player(player1)

    def test_remoe(self):
        game = UltraCricketGame()
        player1 = HyperIntelligentBeing("Зорас", 5000)
        game.remove_player(player1)
        
if __name__ == "__main__":
    unittest.main()
