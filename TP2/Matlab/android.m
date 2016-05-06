% -------------------
% read data from file

dataset = importdata('wifi_predictions.txt');
if ~isempty(dataset)
    
    % ---------------------------------------------------
    % parse the dataset from string to translatable codes
    
    [expected_y, predicted_y, num_neighbours, var_codes] = parse_predictions(dataset);
    
    unique_num_neighbours = unique(num_neighbours);
    
    for num = 1:length(unique_num_neighbours)
        
        idx = find(num_neighbours == num);
        expected_y_num_neighbours = expected_y(idx);
        predicted_y_num_neighbours = predicted_y(idx);
        
        used_num_neighbours = var_codes{unique_num_neighbours(num), 2};
        title_str = sprintf('Android %s Nearest Neighbours', used_num_neighbours);
        
        android_accuracy = classification_performance(expected_y_num_neighbours, predicted_y_num_neighbours, title_str, var_codes(:, 1));
        
        figure;
        plot(android_accuracy)

        ax = gca;
        ax.XTickLabel = var_codes(:, 1);
        ax.XTickLabelRotation = 90;
        ax.XTick = 1:length(ax.XTickLabel);
        title( sprintf('Android Classification Accuracy for %s Nearest Neighbours', used_num_neighbours))
        xlabel('Class')
        ylabel('Accuracy')
        
    end
    
    pause;
    
else
    
    disp('wifi_predictions.txt has no gathered data.');
    
end

% -------------------
% read data from file

dataset = importdata('wifi_predictions_no_orientation.txt');
if ~isempty(dataset)
    
    % ---------------------------------------------------
    % parse the dataset from string to translatable codes
    
    [expected_y, predicted_y, num_neighbours, var_codes] = parse_predictions(dataset);
    
    unique_num_neighbours = unique(num_neighbours);
    
    for num = 1:length(unique_num_neighbours)
        
        idx = find(num_neighbours == num);
        expected_y_num_neighbours = expected_y(idx);
        predicted_y_num_neighbours = predicted_y(idx);
        
        used_num_neighbours = var_codes{unique_num_neighbours(num), 2};
        title_str = sprintf('Android %s Nearest Neighbours', used_num_neighbours);
        
        android_accuracy = classification_performance(expected_y_num_neighbours, predicted_y_num_neighbours, title_str, var_codes(:, 1));
        
        figure;
        plot(android_accuracy)

        ax = gca;
        ax.XTickLabel = var_codes(:, 1);
        ax.XTickLabelRotation = 90;
        ax.XTick = 1:length(ax.XTickLabel);
        title( sprintf('Android Classification Accuracy for %s Nearest Neighbours', used_num_neighbours))
        xlabel('Class')
        ylabel('Accuracy')
        
    end
    
    pause;
    
else
    
    disp('wifi_predictions_no_orientation.txt has no gathered data.');
    
end


%EOF