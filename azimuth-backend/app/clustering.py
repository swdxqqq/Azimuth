from typing import Tuple

import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
from sklearn.cluster import KMeans
from sklearn.metrics import silhouette_score
from sklearn.decomposition import PCA
from app import data_analysis as da


def prepare_clustering_data(year: int) -> Tuple[pd.DataFrame, np.ndarray]:
    """Prepare data for clustering for the specific year"""
    df_year = da.get_year_snapshot(year)
    df_cut = da.remove_excessive_features(df_year)
    df_normalized = da.normalize_data(df=df_cut, method="standard")
    
    # Find numeric columns for featuring
    feature_cols = [col for col in df_normalized.select_dtypes(include=['number']).columns
                    if col != 'Year']
    
    X = df_normalized[feature_cols].values
    
    return df_normalized, X


def find_optimal_k(X: np.ndarray, max_k: int = 10) -> None:
    """Build Elbow Method plot to find optimal K-value"""
    inertias = []
    silhouette_scores = []
    k_range = range(2, max_k + 1)
    
    for k in k_range:
        kmeans = KMeans(n_clusters=k, init="k-means++", random_state=42, n_init=20)
        kmeans.fit(X)
        
        inertias.append(kmeans.inertia_)
        silhouette_scores.append(silhouette_score(X, kmeans.labels_))
    
    plt.figure(figsize=(12, 5))
    
    plt.subplot(1, 2, 1)
    plt.plot(k_range, inertias, marker="o")
    plt.xlabel("Number of clusters (K)")
    plt.ylabel("Inertia")
    plt.title("Elbow Method")
    plt.grid(True)
    
    plt.subplot(1, 2, 2)
    plt.plot(k_range, silhouette_scores, marker="o")
    plt.xlabel("Number of clusters (K)")
    plt.ylabel("Silhouette score")
    plt.grid(True)
    
    plt.tight_layout()
    plt.show()
    
    print("\nK | Inertia | Silhouette Score")
    for k, inertia, sil in zip(k_range, inertias, silhouette_scores):
        print(f"{k} | {inertia:.2f} | {sil:.2f}")


def visualize_clusters_pca(X: np.ndarray, n_clusters: int, labels: np.ndarray = None) -> None:
    """Visualize clustering results in 2D using PCA dimensionality reduction"""
    # PCA reduction to 2 components
    pca = PCA(n_components=2)
    X_pca = pca.fit_transform(X)
    
    # If labels not provided, perform K-Means
    if labels is None:
        kmeans = KMeans(n_clusters=n_clusters, init="k-means++", random_state=42, n_init=20)
        labels = kmeans.fit_predict(X)
        
        # Transform centroids to PCA space
        centroids_pca = pca.transform(kmeans.cluster_centers_)
    else:
        centroids_pca = None
    
    # Create scatter plot
    plt.figure(figsize=(10, 7))
    scatter = plt.scatter(X_pca[:, 0], X_pca[:, 1], c=labels, cmap='viridis', 
                         s=100, alpha=0.6, edgecolors='k', linewidth=0.5)
    
    # Plot centroids if available
    if centroids_pca is not None:
        plt.scatter(centroids_pca[:, 0], centroids_pca[:, 1], 
                   c='red', marker='X', s=300, edgecolors='black', linewidth=2,
                   label='Centroids')
        plt.legend()
    
    # Add labels and title
    variance_explained = pca.explained_variance_ratio_
    plt.xlabel(f'PC1 ({variance_explained[0]:.1%} variance)')
    plt.ylabel(f'PC2 ({variance_explained[1]:.1%} variance)')
    plt.title(f'K-Means Clustering (K={n_clusters}) - PCA Visualization\n'
             f'Total variance explained: {sum(variance_explained):.1%}')
    plt.colorbar(scatter, label='Cluster')
    plt.grid(True, alpha=0.3)
    plt.tight_layout()
    plt.show()        
        
def main():
    df_year, X = prepare_clustering_data(2025)
    find_optimal_k(X) 
    for k in [3, 4, 5]:
        visualize_clusters_pca(X, n_clusters=k)0
        
if __name__ == '__main__':
    main()