#include <iostream>
#include <deque>
#include <unordered_map>
#include <vector>

int main() {
    std::string input;
    std::cin >> input;
    std::deque<char> processQueue;
    std::unordered_map<int, int> matchPairs;
    int animalTracker = 0, matchCounter = 0;
    std::vector<int> animalPositions, trapPositions;

    for (int i = 0; i < input.size(); ++i) {
        char current = input[i];
        bool isAnimal = islower(current);

        if (isAnimal) {
            animalTracker++;
            animalPositions.push_back(animalTracker);
        } else {
            int offset = i - animalTracker;
            trapPositions.push_back(offset);
        }

        if (!processQueue.empty() && tolower(current) == tolower(processQueue.back())) {
            if (current != processQueue.back()) {
                matchPairs[trapPositions.back()] = animalPositions.back();
                trapPositions.pop_back();
                animalPositions.pop_back();
                processQueue.pop_back();
                matchCounter++;
            } else {
                processQueue.push_back(current);
            }
        } else {
            processQueue.push_back(current);
        }
    }

    if (processQueue.empty()) {
        std::cout << "Possible" << std::endl;
        for (int i = 0; i < matchCounter; ++i) {
            std::cout << matchPairs[i] << " ";
        }
        std::cout << std::endl;
    } else {
        std::cout << "Impossible" << std::endl;
    }

    return 0;
}
