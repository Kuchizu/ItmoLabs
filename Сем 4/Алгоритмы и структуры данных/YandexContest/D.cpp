#include <bits/stdc++.h>

using namespace std;

int main() {

    int a, b, c, d, k, x, old;
    cin>>a>>b>>c>>d>>k;
    for(int i = 0; i < k; i++, old=a){
        x = a * b;
        if(x <= c){
            cout<<0;
            return 0;
        }
        x -= c;
        a = min(x, d);
        if(a == old){
            break;
        }
    }
    cout<<a;

    return 0;
}
