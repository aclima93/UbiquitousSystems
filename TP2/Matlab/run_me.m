close all;
clc;

% -------------------
% read data from file

dataset = importdata('wifi_logs.txt');
if ~isempty(dataset)
    
    % ---------------------------------------------------
    % parse the dataset from string to translatable codes
    
    [X, y, var_codes] = parse_data(dataset);
    
    % ---------------------------------------------------
    % split the dataset into training and testing subsets
    
    [ train_X, test_X, train_y, test_y ] = split_data( X', y', 0.70 );
    
    run('neural_network');
    
    run('decision_tree');
    
    run('android');
    
else
    
    disp('wifi_logs.txt has no gathered data.');
    
end
%EOF