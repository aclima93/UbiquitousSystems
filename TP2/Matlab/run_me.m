close all;
clc;

% ------------------------------------------
% Analyse data collected with android device
run('android');

% -------------------------------------
% read data from file with orientations

dataset = importdata('wifi_logs.txt');
if ~isempty(dataset)
    
    % ---------------------------------------------------
    % parse the dataset from string to translatable codes
    
    [X, y, var_codes] = parse_data(dataset);
    
    % ---------------------------------------------------
    % split the dataset into training and testing subsets
    
    [ train_X, test_X, train_y, test_y ] = split_data( X', y', 0.70 );
    
    run('neural_network');
    
    pause;
    
    run('decision_tree');
    
    pause;
    
else
    
    disp('wifi_logs.txt has no gathered data.');
    
end

% ---------------------------------------
% read data from file without orientaions

dataset = importdata('wifi_logs_no_orientation.txt');
if ~isempty(dataset)
    
    % ---------------------------------------------------
    % parse the dataset from string to translatable codes
    
    [X, y, var_codes] = parse_data(dataset);
    
    % ---------------------------------------------------
    % split the dataset into training and testing subsets
    
    [ train_X, test_X, train_y, test_y ] = split_data( X', y', 0.70 );
    
    run('neural_network');
    
    pause;
    
    run('decision_tree');
  
else
    
    disp('wifi_logs_no_orientation.txt has no gathered data.');
    
end

%EOF