#include <bits/stdc++.h>

using namespace std;

int main(){

    int n, c;
    cin>>n;

    vector<int> v(n);
    map <int, int> mp;
    pair <pair <int, int>, int> idx = {{0, 0}, 0};

    for(int i = 0; i < n; i++){
        cin>>v[i];
    }
    for(int i = 0; i < n; i++){
        c = 0;
        mp.clear();
        for(int j = i; j < n; j++){
            mp[v[j]]++;
            if(mp[v[j]] == 3){
                break;
            }
            c++;
            if(c > idx.second){
                idx.first = {i + 1, j + 1};
                idx.second = c;
            }
        }
    }
    cout<<idx.first.first<<" "<<idx.first.second;

    return 0;
}