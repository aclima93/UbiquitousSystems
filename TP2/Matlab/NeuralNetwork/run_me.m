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
    
    % -----------------------
    % train the neural network
    
    hidden_layers = ceil(log(length(unique(train_y))));
    net = feedforwardnet( hidden_layers, 'trainlm' );
    net = train(net, train_X, train_y);
    
    % ----------------------
    % test the neural network
    
    predicted_y = net(test_X);
    %round to nearest class
    predicted_y = round(predicted_y);
    
    % ----------------
    % confusion matrix
    
    C = confusionmat(test_y, predicted_y);
    
else
    
    disp('File has no gathered data.');
    
end
%EOF