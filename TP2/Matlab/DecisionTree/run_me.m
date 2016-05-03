close all;
clc;

% -------------------
% read data from file

dataset = importdata('wifi_logs.txt');
if length(dataset) > 0
    
    % ---------------------------------------------------
    % parse the dataset from string to translatable codes
    
    [X, y, var_codes] = parse_data(dataset);
    
    % ---------------------------------------------------
    % split the dataset into training and testing subsets
    
    [ train_X, test_X, train_y, test_y ] = split_data( X', y', 0.70 );
    
    % for function compatibility reasons
    train_X = train_X'; test_X = test_X'; train_y = train_y'; test_y = test_y';
    
    % -----------------------
    % train the decision tree
    
    tree = fitctree(train_X, train_y);
    view(tree,'Mode','Graph');
    
    % ----------------------
    % test the decision tree
    
    predicted_y = predict(tree, test_X);
    
    % ----------------
    % confusion matrix
    
    plotconfusion(test_y,predicted_y);
    
else
    
    disp('File is empty');
    
end
%EOF