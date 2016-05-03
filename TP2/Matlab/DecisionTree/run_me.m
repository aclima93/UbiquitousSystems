% -------------------
% read data from file

dataset = importdata('wifi_logs.txt');

len_data = length(dataset);
if len_data > 0
    
    num_vars = length(strsplit(dataset{1}, ','));
    split_dataset = cell(len_data, num_vars);
    
    % --------------------------------------------------------
    % split data at commas: <RSSI>,<BSSID>,<Expected Location>

    for measurement_idx = 1:len_data
        temp = strsplit(dataset{measurement_idx}, ',');
        
        for variable_idx = 1:num_vars
            split_dataset{measurement_idx, variable_idx} = temp{variable_idx};
        end
    end
    
    % the variables are all columns except the last one
    X = zeros(len_data, num_vars - 1);
    for variable_idx = 1:num_vars-1
        
        unique_var_vals = unique(split_dataset(:, variable_idx));
        
        for value_idx = 1:length(unique_var_vals)
            for measurement_idx = 1:len_data
                
                % since matlab's implementation does not accept strings,
                % we must code each measurement accordingly
                
                if strcmp(split_dataset{measurement_idx, variable_idx}, unique_var_vals{value_idx})
                    X( measurement_idx, variable_idx) = value_idx;
                end
                
            end
        end
    end
    
    % the expected output is the last column
    y = split_dataset(:, end);
    
    % ---------------------------------------------------
    % split the dataset into training and testing subsets
    
    
    % -----------------------
    % train the decision tree
    
    tree = fitctree(train_X, train_y);
    
    figure;
    view(tree,'Mode','Graph')
    
    % ----------------------
    % test the decision tree
    
    predicted_y = predict(tree, test_X);
    
    % ----------------
    % confusion matrix
    
end