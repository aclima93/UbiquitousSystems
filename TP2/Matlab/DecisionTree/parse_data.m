function [ X, y, var_codes ] = parse_data( dataset )
%PARSE_DATA Parse the dataset from string to translatable codes

    len_data = length(dataset);
    num_vars = length(strsplit(dataset{1}, ','));
    split_dataset = cell(len_data, num_vars);
    
    % split data at commas: <RSSI>,<BSSID>,<Expected Location>

    for measurement_idx = 1:len_data
        temp = strsplit(dataset{measurement_idx}, ',');
        
        for variable_idx = 1:num_vars
            split_dataset{measurement_idx, variable_idx} = temp{variable_idx};
        end
    end
    
    % convert all string values to an id code for easier processing in
    % matlab's awkward object definitions
    
    var_codes = cell(num_vars, 1);
    X = zeros(len_data, num_vars);
    for variable_idx = 1:num_vars
        
        unique_var_vals = unique(split_dataset(:, variable_idx),'sorted');
        
        for value_idx = 1:length(unique_var_vals)

            var_codes{value_idx, variable_idx} = unique_var_vals{value_idx};
            
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
    X = X(:, 1:end-1);
    y = X(:, end);

end

