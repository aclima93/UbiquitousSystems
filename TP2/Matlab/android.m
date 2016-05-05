% -------------------
% read data from file

dataset = importdata('wifi_predictions.txt');
if ~isempty(dataset)
    
    % ---------------------------------------------------
    % parse the dataset from string to translatable codes
    
    [expected_y, predicted_y, num_neighbours, var_codes] = parse_predictions(dataset);
    
    for num = 1:length(unique(num_neighbours))
        
        idx = num_neighbours(num_neighbours == num);
        expected_y_num_neighbours = expected_y(idx);
        predicted_y_num_neighbours = predicted_y(idx);
        
        android_accuracy = classification_performance(expected_y_num_neighbours, predicted_y_num_neighbours);

        figure;
        plot(android_accuracy);
        title( sprintf('Android Classification Accuracy for %s Nearest Neighbours', var_codes{num, 2}))
        xlabel('Class')
        ylabel('Accuracy')
    
    end
    
else
    
    disp('File has no gathered data.');
    
end
%EOF