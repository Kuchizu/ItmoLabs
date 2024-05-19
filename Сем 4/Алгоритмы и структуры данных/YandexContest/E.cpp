#include <iostream>
#include <vector>

using namespace std;

class CowStalls {
public:
    CowStalls(const vector<int>& stalls) : stalls(stalls) {}

    int getMaxMinDistance(int k) {
        int left = 1;
        int right = stalls[stalls.size() - 1] - stalls[0];
        int ans = 0;
        while (left <= right) {
            int mid = left + (right - left) / 2;
            if (isPossible(k, mid)) {
                ans = mid;
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        return ans;
    }

private:
    vector<int> stalls;

    bool isPossible(int k, int dist) {
        int count = 1;
        int lastPos = stalls[0];
        for (int i = 1; i < stalls.size(); ++i) {
            if (stalls[i] - lastPos >= dist) {
                lastPos = stalls[i];
                ++count;
                if (count >= k) return true;
            }
        }
        return false;
    }
};

int main() {
    int n, k;
    cin >> n >> k;
    vector<int> stalls(n);
    for (int i = 0; i < n; ++i) {
        cin >> stalls[i];
    }

    CowStalls cowStalls(stalls);
    cout << cowStalls.getMaxMinDistance(k) << endl;

    return 0;
}
