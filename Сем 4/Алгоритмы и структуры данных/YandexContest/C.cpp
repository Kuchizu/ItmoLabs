#include <bits/stdc++.h>

using namespace std;

auto newScope() -> vector<string> {
    return vector<string>{};
}

int main() {
    map<string, stack<int>> valueStack;
    stack<vector<string>> modifiedVars;
    modifiedVars.push(newScope());

    string operation;
    while (getline(cin, operation)) {
        if (operation[0] == '{') {
            modifiedVars.push(newScope());
        } else if (operation[0] == '}') {
            for (const auto& var : modifiedVars.top()) {
                valueStack[var].pop();
            }
            modifiedVars.pop();
        } else {
            auto separator = operation.find('=');
            string varName = operation.substr(0, separator);
            string valueOrVar = operation.substr(separator + 1);

            if (isdigit(valueOrVar[0]) || valueOrVar[0] == '-') {
                valueStack[varName].push(stoi(valueOrVar));
            } else {
                if (valueStack[valueOrVar].empty()) valueStack[valueOrVar].push(0);
                valueStack[varName].push(valueStack[valueOrVar].top());
                cout << valueStack[varName].top() << endl;
            }
            modifiedVars.top().push_back(varName);
        }
    }

    return 0;
}
