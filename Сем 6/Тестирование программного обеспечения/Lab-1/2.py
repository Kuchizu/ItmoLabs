import unittest

def dfs(graph, start, visited=None, order=None):
    if visited is None:
        visited = set()
    if order is None:
        order = []

    visited.add(start)
    order.append(start)

    if not graph.get(start):
        return visited, order

    for neighbor in graph[start]:
        if neighbor not in visited:
            dfs(graph, neighbor, visited, order)

    return visited, order

class TestDFS(unittest.TestCase):
    def test_dfs_graph1(self):
        graph1 = {
            'A': ['B', 'C'],
            'B': ['A', 'D', 'E'],
            'C': ['A'],
            'D': ['B'],
            'E': ['B']
        }
        visited, order = dfs(graph1, 'A')
        self.assertEqual(visited, {'A', 'B', 'C', 'D', 'E'}, f"Unexpected DFS result for graph1: {visited}")
        self.assertEqual(order, ['A', 'B', 'D', 'E', 'C'], f"Unexpected DFS order for graph1: {order}")

    def test_dfs_graph2(self):
        graph2 = {
            'A': ['B'],
            'B': ['C'],
            'C': ['A']
        }
        visited, order = dfs(graph2, 'A')
        self.assertEqual(visited, {'A', 'B', 'C'}, f"Unexpected DFS result for graph2: {visited}")
        self.assertEqual(order, ['A', 'B', 'C'], f"Unexpected DFS order for graph2: {order}")

    def test_dfs_graph3(self):
        graph3 = {
            'A': ['B'],
            'B': ['A'],
            'C': ['D'],
            'D': ['C']
        }
        visited, order = dfs(graph3, 'A')
        self.assertEqual(visited, {'A', 'B'}, f"Unexpected DFS result for graph3: {visited}")
        self.assertEqual(order, ['A', 'B'], f"Unexpected DFS order for graph3: {order}")

    def test_dfs_empty_graph(self):
        graph4 = {}
        visited, order = dfs(graph4, 'A')
        self.assertEqual(visited, {'A'}, f"Unexpected DFS result for empty graph: {visited}")
        self.assertEqual(order, ['A'], f"Unexpected DFS order for empty graph: {order}")

    def test_dfs_single_node(self):
        graph5 = {
            'A': []
        }
        visited, order = dfs(graph5, 'A')
        self.assertEqual(visited, {'A'}, f"Unexpected DFS result for single node graph: {visited}")
        self.assertEqual(order, ['A'], f"Unexpected DFS order for single node graph: {order}")

    def test_dfs_isolated_node(self):
        graph6 = {
            'A': ['B'],
            'B': ['A'],
            'C': []
        }
        visited, order = dfs(graph6, 'A')
        self.assertEqual(visited, {'A', 'B'}, f"Unexpected DFS result for graph with isolated node: {visited}")
        self.assertEqual(order, ['A', 'B'], f"Unexpected DFS order for graph with isolated node: {order}")

    def test_dfs_disconnected_graph(self):
        graph7 = {
            'A': ['B'],
            'B': [],
            'C': ['D'],
            'D': []
        }
        visited, order = dfs(graph7, 'A')
        self.assertEqual(visited, {'A', 'B'}, f"Unexpected DFS result for disconnected graph: {visited}")
        self.assertEqual(order, ['A', 'B'], f"Unexpected DFS order for disconnected graph: {order}")
    
    def test_dfs_graph_with_cycle(self):
        graph8 = {
            'A': ['B'],
            'B': ['C'],
            'C': ['A']
        }
        visited, order = dfs(graph8, 'A')
        self.assertEqual(visited, {'A', 'B', 'C'}, f"Unexpected DFS result for graph with cycle: {visited}")
        self.assertEqual(order, ['A', 'B', 'C'], f"Unexpected DFS order for graph with cycle: {order}")

    def test_dfs_non_existent_node(self):
        graph9 = {
            'A': ['B'],
            'B': ['C'],
            'C': []
        }
        visited, order = dfs(graph9, 'D')
        self.assertEqual(visited, {'D'}, f"Unexpected DFS result for non-existent node: {visited}")
        self.assertEqual(order, ['D'], f"Unexpected DFS order for non-existent node: {order}")

    def test_dfs_start_at_non_first_node(self):
        graph10 = {
            'A': ['B'],
            'B': ['C'],
            'C': ['D'],
            'D': []
        }
        visited, order = dfs(graph10, 'B')
        self.assertEqual(visited, {'B', 'C', 'D'}, f"Unexpected DFS result for starting from B: {visited}")
        self.assertEqual(order, ['B', 'C', 'D'], f"Unexpected DFS order for starting from B: {order}")

    def test_dfs_no_edges(self):
        graph11 = {
            'A': [],
            'B': [],
            'C': []
        }
        visited, order = dfs(graph11, 'A')
        self.assertEqual(visited, {'A'}, f"Unexpected DFS result for graph with no edges: {visited}")
        self.assertEqual(order, ['A'], f"Unexpected DFS order for graph with no edges: {order}")

if __name__ == "__main__":
    unittest.main()
