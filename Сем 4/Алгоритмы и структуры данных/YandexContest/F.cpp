#include <bits/stdc++.h>

using namespace std;

bool isGreater(const string &a, const string &b) {
    for (int i = 0; i < min(a.size(), b.size()); ++i) {
        if (a[i] != b[i]) {
            return a[i] > b[i];
        }
    }
    return a.size() > b.size();
}

int main() {
    vector<string> v;

    string s;
    while(cin>>s){
        v.push_back(s);
    }

    sort(v.begin(), v.end(), [](const string &a, const string &b) {
        return isGreater(a + b, b + a);
    });

    for (const string &part : v) {
        cout << part;
    }

    return 0;
}
