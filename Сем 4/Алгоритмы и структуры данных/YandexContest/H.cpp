#include <iostream>
#include <vector>
#include <algorithm>

class BillShopping {
private:
    std::vector<int> prices;
    int k;

public:
    BillShopping(const std::vector<int>& prices, int k) : prices(prices), k(k) {}

    int calculateMinimumExpense() {
        std::sort(prices.begin(), prices.end(), std::greater<int>());

        int totalCost = 0;
        int freeItems = 0;

        for (int i = 0; i < prices.size(); ++i) {
            if ((i + 1) % k != 0) {
                totalCost += prices[i];
            } else {
                freeItems++;
            }
        }

        return totalCost;
    }
};

int main() {
    int n, k;
    std::cin >> n >> k;
    std::vector<int> prices(n);
    for (int i = 0; i < n; ++i) {
        std::cin >> prices[i];
    }

    BillShopping shopper(prices, k);
    std::cout << shopper.calculateMinimumExpense() << std::endl;

    return 0;
}