#include <iostream>
int main() {
    int n, pv = -1, cv = -1, v, s = 1, e = 1, m = 0, ts = 1;
    std::cin >> n;
    for (int i = 1; i <= n; i++) {
        std::cin >> v;
        if (v == pv && v == cv && pv > 0) ts = i - 1;
        if (i - ts + 1 > m) {
            m = i - ts + 1;
            s = ts;
            e = i;
        }
        pv = cv;
        cv = v;
    }
    std::cout << s << " " << e << std::endl;
    return 0;
}
