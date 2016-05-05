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

% ---------------------------------
% analise performance of classifier

neural_network_accuracy = classification_performance(test_y, predicted_y);

figure;
plot(neural_network_accuracy);
title('Neural Network Accuracy')
xlabel('Class')

%EOF