function [ train_X, test_X, train_y, test_y ] = split_data( X, y, training_ratio )
%SPLIT_DATA Create a stratified training dataset
% 70% of 0 class samples and 70% of 1 class samples for training and
% vice versa for testing. Random sampling can bias the classifier's
% prediction.

train_X = [];
test_X = [];
train_y = [];
test_y = [];

% randomly shuffle dataset before splitting it
rand_idx = randperm(size(X,2));
X = X(:, rand_idx);
y = y(:, rand_idx);

for class_label = unique(y)
    
    % indexes of stratifyed data per class label
    % eg: 70% of 0s + 70% of 1s, 30% of 0s + 30% of 1s
    idx = find( y == class_label);
    train_idx = idx(:, 1:round(end*training_ratio));
    test_idx = idx(:, round(end*training_ratio)+1:end);
    
    if isempty(train_X)
        
        train_X = X(:, train_idx);
        test_X = X(:, test_idx);
        train_y = y(train_idx);
        test_y = y(test_idx);
        
    else
        
        train_X = horzcat( train_X, X(:, train_idx));
        test_X = horzcat( test_X, X(:, test_idx));
        train_y = horzcat( train_y, y(train_idx));
        test_y = horzcat( test_y, y(test_idx));
        
    end
    
end

end
% EOF