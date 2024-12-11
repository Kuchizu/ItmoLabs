# Import necessary libraries
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import random
from mpl_toolkits.mplot3d import Axes3D

df = pd.read_csv('WineDataset.csv')
print("\nMissing values in each column:")
print(df.isnull().sum())

# Separate features and target variable
X = df.drop('Wine', axis=1)
y = df['Wine']

# Standard scaling of features
X_scaled = (X - X.mean()) / X.std()

# 2. Get and Visualize Statistics

# Compute statistics
print("\nStatistical summary of the dataset:")
stats = df.describe()
print(stats)

# Histograms for each feature
print("\nPlotting histograms for each feature...")
X_scaled.hist(bins=20, figsize=(15, 10))
plt.tight_layout()
plt.show()

# 3D Visualization of features
print("\n3D Visualization of features...")
fig = plt.figure(figsize=(10, 8))
ax = fig.add_subplot(111, projection='3d')

x = X_scaled['Alcohol']
y_3d = X_scaled['Malic Acid']
z = X_scaled['Ash']
c = df['Wine']  # Color by wine class

scatter = ax.scatter(x, y_3d, z, c=c, cmap='viridis')

ax.set_xlabel('Alcohol')
ax.set_ylabel('Malic Acid')
ax.set_zlabel('Ash')

legend1 = ax.legend(*scatter.legend_elements(),
                    loc="upper right", title="Wine Class")
ax.add_artist(legend1)

plt.show()


# 3. Implement KNN Method Without Using Third-Party Libraries

def knn_predict(X_train, y_train, X_test_instance, k):
    # Compute distances between X_test_instance and all X_train instances
    distances = np.sqrt(np.sum((X_train - X_test_instance) ** 2, axis=1))

    # Get the indices of the k nearest neighbors
    k_indices = distances.argsort()[:k]

    # Get the labels of the k nearest neighbors
    k_nearest_labels = y_train.iloc[k_indices]

    # Majority vote
    most_common = k_nearest_labels.mode()[0]
    return most_common


# 4. Build Two KNN Models with Different Feature Sets

# Shuffle and split the data into training and testing sets
data = X_scaled.copy()
data['Wine'] = df['Wine']

data = data.sample(frac=1, random_state=42).reset_index(drop=True)

# Split features and target
X_shuffled = data.drop('Wine', axis=1)
y_shuffled = data['Wine']

# Split into training and testing sets
split_index = int(0.8 * len(X_shuffled))

X_train = X_shuffled.iloc[:split_index].reset_index(drop=True)
y_train = y_shuffled.iloc[:split_index].reset_index(drop=True)

X_test = X_shuffled.iloc[split_index:].reset_index(drop=True)
y_test = y_shuffled.iloc[split_index:].reset_index(drop=True)

# Model 1: Randomly select 5 features
random_features = random.sample(list(X_train.columns), 5)
X_train_model1 = X_train[random_features]
X_test_model1 = X_test[random_features]

# Model 2: Fixed set of features
fixed_features = ['Alcohol', 'Malic Acid', 'Ash']
X_train_model2 = X_train[fixed_features]
X_test_model2 = X_test[fixed_features]


# 5. Evaluate Each Model

def evaluate_knn(X_train, y_train, X_test, y_test, k):
    y_pred = []
    for i in range(len(X_test)):
        X_test_instance = X_test.iloc[i].values
        prediction = knn_predict(X_train.values, y_train, X_test_instance, k)
        y_pred.append(prediction)
    y_pred = np.array(y_pred)

    # Compute confusion matrix
    classes = np.unique(y_test)
    confusion_matrix = pd.DataFrame(0, index=classes, columns=classes)
    for actual, predicted in zip(y_test, y_pred):
        confusion_matrix.loc[actual, predicted] += 1
    return confusion_matrix, y_pred


# Evaluate Model 1
ks = [3, 5, 10]
print("\nEvaluating Model 1 (Random Features):")
for k in ks:
    print(f"\nModel 1 with k={k}")
    confusion_matrix, y_pred = evaluate_knn(X_train_model1, y_train, X_test_model1, y_test, k)
    print('Confusion Matrix:')
    print(confusion_matrix)

# Evaluate Model 2
print("\nEvaluating Model 2 (Fixed Features):")
for k in ks:
    print(f"\nModel 2 with k={k}")
    confusion_matrix, y_pred = evaluate_knn(X_train_model2, y_train, X_test_model2, y_test, k)
    print('Confusion Matrix:')
    print(confusion_matrix)
